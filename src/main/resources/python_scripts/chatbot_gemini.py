# chatbot_gemini.py
import sys
import json
import os
import google.generativeai as genai
# Import các hàm truy vấn DB của bạn từ file db_queries.py hoặc định nghĩa trực tiếp tại đây
# from db_queries import get_total_products, get_best_selling_product, ...

# Hàm kết nối DB và các hàm truy vấn DB (được copy từ ví dụ trước)
# Bạn nên đặt chúng vào một file riêng như db_utils.py để dễ quản lý và import vào đây
import sqlite3

def get_db_connection():
    conn = sqlite3.connect('jobfinder.db')
    conn.row_factory = sqlite3.Row
    return conn

def get_total_products():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM products")
    count = cursor.fetchone()[0]
    conn.close()
    return count

def get_best_selling_product():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT name, sales_count FROM products ORDER BY sales_count DESC LIMIT 1")
    result = cursor.fetchone()
    conn.close()
    if result:
        return {"name": result["name"], "sales_count": result["sales_count"]}
    return None

def get_product_details(product_name):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT name, price, description, sales_count FROM products WHERE name LIKE ?", ('%' + product_name + '%',))
    result = cursor.fetchone()
    conn.close()
    if result:
        return dict(result)
    return None

def get_total_jobs():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM jobs")
    count = cursor.fetchone()[0]
    conn.close()
    return count

def get_jobs_by_category(category):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT title, salary FROM jobs WHERE category LIKE ?", ('%' + category + '%',))
    jobs = cursor.fetchall()
    conn.close()
    return [dict(job) for job in jobs]

def get_company_jobs(company_name):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("""
                   SELECT j.title, j.salary
                   FROM jobs j
                            JOIN companies c ON j.company_id = c.id
                   WHERE c.name LIKE ?
                   """, ('%' + company_name + '%',))
    jobs = cursor.fetchall()
    conn.close()
    return [dict(job) for job in jobs]

# Chạy init_db() một lần để đảm bảo database có dữ liệu (có thể bỏ khi deploy)
def init_db():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
                   CREATE TABLE IF NOT EXISTS jobs (
                                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                       title TEXT NOT NULL,
                                                       description TEXT,
                                                       salary INTEGER,
                                                       company_id INTEGER,
                                                       category TEXT,
                                                       FOREIGN KEY (company_id) REFERENCES companies(id)
                   )
                   ''')
    cursor.execute('''
                   CREATE TABLE IF NOT EXISTS companies (
                                                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                            name TEXT NOT NULL,
                                                            address TEXT,
                                                            industry TEXT
                   )
                   ''')
    cursor.execute('''
                   CREATE TABLE IF NOT EXISTS products (
                                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                           name TEXT NOT NULL,
                                                           price REAL,
                                                           description TEXT,
                                                           sales_count INTEGER DEFAULT 0
                   )
                   ''')

    cursor.execute("INSERT OR IGNORE INTO companies (id, name, address, industry) VALUES (1, 'Tech Solutions Inc.', '123 Silicon Valley', 'IT')")
    cursor.execute("INSERT OR IGNORE INTO companies (id, name, address, industry) VALUES (2, 'Creative Marketing LLC', '456 Ad Street', 'Marketing')")
    cursor.execute("INSERT OR IGNORE INTO companies (id, name, address, industry) VALUES (3, 'Global Finance Group', '789 Wall Street', 'Finance')")

    cursor.execute("INSERT OR IGNORE INTO jobs (id, title, salary, company_id, category) VALUES (1, 'Senior Python Developer', 120000, 1, 'Software')")
    cursor.execute("INSERT OR IGNORE INTO jobs (id, title, salary, company_id, category) VALUES (2, 'Marketing Specialist', 75000, 2, 'Marketing')")
    cursor.execute("INSERT OR IGNORE INTO jobs (id, title, salary, company_id, category) VALUES (3, 'Financial Analyst', 90000, 3, 'Finance')")
    cursor.execute("INSERT OR IGNORE INTO jobs (id, title, salary, company_id, category) VALUES (4, 'Junior Fullstack Dev', 80000, 1, 'Software')")

    cursor.execute("INSERT OR IGNORE INTO products (id, name, price, sales_count) VALUES (1, 'Premium Job Posting Package', 99.99, 150)")
    cursor.execute("INSERT OR IGNORE INTO products (id, name, price, sales_count) VALUES (2, 'Resume Review Service', 49.99, 200)")
    cursor.execute("INSERT OR IGNORE INTO products (id, name, price, sales_count) VALUES (3, 'AI Interview Prep Tool', 19.99, 80)")
    cursor.execute("INSERT OR IGNORE INTO products (id, name, price, sales_count) VALUES (4, 'Featured Company Listing', 299.00, 50)")

    conn.commit()
    conn.close()

# Đảm bảo DB được khởi tạo khi script chạy lần đầu
init_db()


# Cấu hình Gemini API
try:
    genai.configure(api_key=os.environ["GOOGLE_API_KEY"])
except KeyError:
    print("Error: GOOGLE_API_KEY environment variable not set.", file=sys.stderr)
    sys.exit(1)

model = genai.GenerativeModel('gemini-pro')

# Hàm chính để xử lý yêu cầu
def handle_gemini_request(user_query):
    system_instruction = """
    Bạn là một trợ lý chatbot thông minh cho dự án JobFinder.
    Nhiệm vụ của bạn là trả lời các câu hỏi về thông tin dự án JobFinder.
    Bạn CÓ KHẢ NĂNG truy cập database của JobFinder thông qua các HÀM sau:
    - `get_total_products()`: Trả về tổng số sản phẩm hiện có.
    - `get_best_selling_product()`: Trả về tên và số lượng bán của sản phẩm bán chạy nhất.
    - `get_product_details(product_name)`: Trả về chi tiết (tên, giá, mô tả, số lượng bán) của một sản phẩm cụ thể.
    - `get_total_jobs()`: Trả về tổng số công việc đang có.
    - `get_jobs_by_category(category)`: Trả về danh sách công việc (tên, mức lương) theo danh mục.
    - `get_company_jobs(company_name)`: Trả về danh sách công việc (tên, mức lương) của một công ty cụ thể.

    Nếu câu hỏi của người dùng có thể được trả lời bằng một trong các hàm trên,
    hãy trả về một đối tượng JSON với cấu trúc:
    {
        "action": "call_function",
        "function_name": "tên_hàm",
        "parameters": {
            "tên_tham_số_1": "giá_trị_tham_số_1",
            "tên_tham_số_2": "giá_trị_tham_số_2"
        }
    }
    Nếu không có tham số, `parameters` sẽ là một object rỗng `{}`.
    Nếu người dùng hỏi về sản phẩm nhưng không rõ tên, hãy trả về `{"action": "ask_for_clarification", "question": "Bạn muốn hỏi về sản phẩm nào?"}`.

    Nếu câu hỏi KHÔNG LIÊN QUAN đến dữ liệu JobFinder hoặc không thể được trả lời bằng các hàm trên,
    hãy trả lời trực tiếp bằng một câu trả lời thân thiện, lịch sự và hữu ích, KHÔNG trả về JSON.

    Ví dụ về các câu hỏi và cách bạn nên trả lời bằng JSON:
    - Hỏi: "Có bao nhiêu sản phẩm của jobfinder?"
      Trả lời: {"action": "call_function", "function_name": "get_total_products", "parameters": {}}
    - Hỏi: "Sản phẩm nào bán chạy nhất vậy?"
      Trả lời: {"action": "call_function", "function_name": "get_best_selling_product", "parameters": {}}
    - Hỏi: "Chi tiết về gói premium job posting?"
      Trả lời: {"action": "call_function", "function_name": "get_product_details", "parameters": {"product_name": "Premium Job Posting Package"}}
    - Hỏi: "Tìm việc làm lập trình"
      Trả lời: {"action": "call_function", "function_name": "get_jobs_by_category", "parameters": {"category": "Software"}}
    - Hỏi: "Có bao nhiêu job vậy?"
      Trả lời: {"action": "call_function", "function_name": "get_total_jobs", "parameters": {}}
    - Hỏi: "job của tech solutions inc"
      Trả lời: {"action": "call_function", "function_name": "get_company_jobs", "parameters": {"company_name": "Tech Solutions Inc."}}
    - Hỏi: "Dịch vụ của bạn là gì?"
      Trả lời: {"action": "ask_for_clarification", "question": "Bạn muốn hỏi về sản phẩm nào?"}

    Hãy ĐẢM BẢO output của bạn là JSON HỢP LỆ nếu bạn chọn `call_function` hoặc `ask_for_clarification`.
    Nếu không phải JSON, hãy trả lời bằng văn bản thuần túy.
    """

    try:
        response = model.generate_content(
            [system_instruction, user_query],
            generation_config=genai.types.GenerationConfig(
                temperature=0.0
            )
        )
        response_text = response.text.strip()

        if response_text.startswith('{') and response_text.endswith('}'):
            try:
                action_data = json.loads(response_text)
                action = action_data.get("action")

                if action == "call_function":
                    function_name = action_data.get("function_name")
                    parameters = action_data.get("parameters", {})

                    result = None
                    if function_name == "get_total_products":
                        result = get_total_products()
                        context = f"Tổng số sản phẩm trong JobFinder là: {result}."
                    elif function_name == "get_best_selling_product":
                        product = get_best_selling_product()
                        if product:
                            context = f"Sản phẩm bán chạy nhất là '{product['name']}' với {product['sales_count']} lượt bán."
                        else:
                            context = "Không tìm thấy sản phẩm bán chạy nhất."
                    elif function_name == "get_product_details":
                        product_name = parameters.get("product_name")
                        if product_name:
                            details = get_product_details(product_name)
                            if details:
                                context = (f"Chi tiết sản phẩm '{details['name']}: "
                                           f"Giá: {details['price']} USD, "
                                           f"Mô tả: {details['description']}, "
                                           f"Lượt bán: {details['sales_count']}."
                                           )
                            else:
                                context = f"Không tìm thấy thông tin cho sản phẩm '{product_name}'."
                        else:
                            context = "Xin lỗi, tôi cần tên sản phẩm để tra cứu chi tiết."
                    elif function_name == "get_total_jobs":
                        result = get_total_jobs()
                        context = f"Tổng số công việc hiện có trong JobFinder là: {result}."
                    elif function_name == "get_jobs_by_category":
                        category = parameters.get("category")
                        if category:
                            jobs = get_jobs_by_category(category)
                            if jobs:
                                job_list = "\n".join([f"- {job['title']} (Mức lương: {job['salary']:,} USD)" for job in jobs])
                                context = f"Các công việc trong danh mục '{category}':\n{job_list}"
                            else:
                                context = f"Không tìm thấy công việc trong danh mục '{category}'."
                        else:
                            context = "Xin lỗi, tôi cần danh mục công việc để tra cứu."
                    elif function_name == "get_company_jobs":
                        company_name = parameters.get("company_name")
                        if company_name:
                            jobs = get_company_jobs(company_name)
                            if jobs:
                                job_list = "\n".join([f"- {job['title']} (Mức lương: {job['salary']:,} USD)" for job in jobs])
                                context = f"Các công việc của công ty '{company_name}':\n{job_list}"
                            else:
                                context = f"Không tìm thấy công việc cho công ty '{company_name}'."
                        else:
                            context = "Xin lỗi, tôi cần tên công ty để tra cứu công việc."
                    else:
                        context = "Tôi không thể thực hiện yêu cầu này. Có vẻ có lỗi trong việc ánh xạ hàm."

                    # Gửi lại câu hỏi ban đầu + ngữ cảnh từ DB đến Gemini để tạo câu trả lời tự nhiên
                    final_prompt = f"Dựa trên thông tin sau từ cơ sở dữ liệu JobFinder:\n\n{context}\n\nTrả lời câu hỏi của người dùng: '{user_query}' một cách tự nhiên và hữu ích."
                    final_response = model.generate_content(final_prompt)
                    return final_response.text

                elif action == "ask_for_clarification":
                    return action_data.get("question", "Tôi cần thêm thông tin để giúp bạn.")

            except json.JSONDecodeError:
                # Nếu Gemini không trả về JSON hợp lệ, coi đó là câu trả lời trực tiếp
                return response_text
        else:
            # Nếu Gemini không trả về JSON, tức là nó đã trả lời trực tiếp
            return response_text

    except Exception as e:
        # Ghi log lỗi vào stderr để Java có thể đọc được
        print(f"Error occurred in Python script: {e}", file=sys.stderr)
        return "Xin lỗi, tôi gặp vấn đề khi xử lý yêu cầu của bạn. Vui lòng thử lại sau."


if __name__ == "__main__":
    if len(sys.argv) > 1:
        user_query = sys.argv[1]
        response = handle_gemini_request(user_query)
        print(response) # In kết quả ra stdout để Java đọc
    else:
        print("Usage: python chatbot_gemini.py <user_query>", file=sys.stderr)