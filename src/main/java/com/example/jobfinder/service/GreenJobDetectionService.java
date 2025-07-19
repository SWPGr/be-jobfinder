package com.example.jobfinder.service;

import com.example.jobfinder.dto.green.GreenJobAnalysis;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GreenJobDetectionService {
    private final StanfordCoreNLP pipeline;

    private static final Map<String, Set<String>> GREEN_CATEGORIES = Map.of(
            "RENEWABLE_ENERGY", Set.of(
                    "solar", "wind", "renewable", "clean energy", "photovoltaic", "turbine",
                    "hydroelectric", "geothermal", "biomass", "energy efficiency", "green energy",
                    "năng lượng tái tạo", "năng lượng xanh", "điện mặt trời", "điện gió"
            ),
            "SUSTAINABILITY", Set.of(
                    "sustainability", "sustainable", "carbon", "emission", "eco-friendly",
                    "green building", "leed", "environmental", "climate", "carbon footprint",
                    "phát triển bền vững", "bền vững", "thân thiện môi trường", "khí thải carbon"
            ),
            "RECYCLING", Set.of(
                    "recycling", "waste management", "circular economy", "reuse", "reduce",
                    "waste reduction", "material recovery", "upcycling", "zero waste",
                    "tái chế", "quản lý chất thải", "kinh tế tuần hoàn", "giảm thiểu chất thải"
            ),
            "CONSERVATION", Set.of(
                    "conservation", "biodiversity", "ecosystem", "wildlife", "forest",
                    "marine", "water conservation", "habitat", "endangered species",
                    "bảo tồn", "đa dạng sinh học", "hệ sinh thái", "động vật hoang dã"
            ),
            "GREEN_TRANSPORT", Set.of(
                    "electric vehicle", "ev", "hybrid", "public transport", "bike", "cycling",
                    "sustainable transport", "emission reduction", "clean transport",
                    "xe điện", "giao thông xanh", "giao thông bền vững", "xe hybrid"
            ),
            "ORGANIC_AGRICULTURE", Set.of(
                    "organic", "permaculture", "sustainable farming", "green agriculture",
                    "eco-farming", "biodynamic", "pesticide-free", "natural farming",
                    "nông nghiệp hữu cơ", "canh tác bền vững", "nông nghiệp xanh"
            )
    );

    private static final Set<String> GREEN_COMPANIES = Set.of(
            "greenpeace", "wwf", "tesla", "vestas", "siemens renewable", "orsted",
            "schneider electric", "johnson controls", "waste management", "veolia",
            "green dragon water", "tập đoàn điện lực việt nam", "petrovietnam gas"
    );

    public GreenJobAnalysis analyzeGreenJob(String jobTitle, String jobDescription, String companyName) {
        if (jobTitle == null && jobDescription == null) {
            return new GreenJobAnalysis(false, 0.0, new ArrayList<>(), new ArrayList<>());
        }

        String combinedText = (jobTitle != null ? jobTitle : "") + " " +
                (jobDescription != null ? jobDescription : "") + " " +
                (companyName != null ? companyName : "");

        // Normalize text
        String normalizedText = normalizeText(combinedText);

        // Extract green keywords
        List<String> detectedKeywords = extractGreenKeywords(normalizedText);

        // Detect green categories
        List<String> detectedCategories = detectGreenCategories(normalizedText);

        // Calculate green score
        double greenScore = calculateGreenScore(normalizedText, detectedKeywords, detectedCategories);

        // Determine if it's a green job
        boolean isGreenJob = greenScore >= 0.3; // Threshold 30%

        return new GreenJobAnalysis(isGreenJob, greenScore, detectedKeywords, detectedCategories);
    }

    /**
     * Trích xuất green keywords từ text
     */
    private List<String> extractGreenKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // Check exact matches
        for (Set<String> categoryKeywords : GREEN_CATEGORIES.values()) {
            for (String keyword : categoryKeywords) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    keywords.add(keyword);
                }
            }
        }


        return keywords.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Detect green categories
     */
    private List<String> detectGreenCategories(String text) {
        List<String> categories = new ArrayList<>();
        String lowerText = text.toLowerCase();

        for (Map.Entry<String, Set<String>> entry : GREEN_CATEGORIES.entrySet()) {
            String category = entry.getKey();
            Set<String> keywords = entry.getValue();

            long matchCount = keywords.stream()
                    .filter(keyword -> lowerText.contains(keyword.toLowerCase()))
                    .count();

            if (matchCount > 0) {
                categories.add(category);
            }
        }

        return categories;
    }

    /**
     * Tính green score cho job
     */
    private double calculateGreenScore(String text, List<String> keywords, List<String> categories) {
        double score = 0.0;
        String lowerText = text.toLowerCase();

        // Base score từ keywords (40%)
        double keywordScore = Math.min(keywords.size() * 0.1, 0.4);
        score += keywordScore;

        // Category diversity score (30%)
        double categoryScore = Math.min(categories.size() * 0.1, 0.3);
        score += categoryScore;

        // Company green score (20%)
        double companyScore = 0.0;
        for (String greenCompany : GREEN_COMPANIES) {
            if (lowerText.contains(greenCompany.toLowerCase())) {
                companyScore = 0.2;
                break;
            }
        }
        score += companyScore;

        // Frequency bonus (10%)
        long totalKeywordOccurrences = GREEN_CATEGORIES.values().stream()
                .flatMap(Set::stream)
                .filter(keyword -> lowerText.contains(keyword.toLowerCase()))
                .count();

        double frequencyScore = Math.min(totalKeywordOccurrences * 0.02, 0.1);
        score += frequencyScore;

        return Math.min(score, 1.0);
    }

    /**
     * Normalize text cho analysis
     */
    private String normalizeText(String text) {
        if (text == null) return "";

        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

        return document.tokens().stream()
                .map(token -> token.lemma().toLowerCase())
                .filter(lemma -> lemma.length() > 2)
                .collect(Collectors.joining(" "));
    }

    /**
     * Lấy green suggestions cho user
     */
    public List<String> getGreenJobSuggestions(String userProfile) {
        List<String> suggestions = new ArrayList<>();
        String normalizedProfile = normalizeText(userProfile);

        // Analyze user interests
        List<String> userGreenCategories = detectGreenCategories(normalizedProfile);

        // Suggest related green job titles
        Map<String, List<String>> categoryJobTitles = Map.of(
                "RENEWABLE_ENERGY", List.of(
                        "Solar Energy Engineer", "Wind Turbine Technician", "Renewable Energy Analyst",
                        "Clean Energy Project Manager", "Energy Efficiency Specialist"
                ),
                "SUSTAINABILITY", List.of(
                        "Sustainability Consultant", "Environmental Analyst", "Carbon Manager",
                        "Green Building Specialist", "Climate Change Analyst"
                ),
                "RECYCLING", List.of(
                        "Waste Management Specialist", "Recycling Coordinator", "Circular Economy Analyst",
                        "Environmental Compliance Officer", "Zero Waste Manager"
                ),
                "CONSERVATION", List.of(
                        "Conservation Biologist", "Environmental Scientist", "Wildlife Manager",
                        "Marine Biologist", "Forest Ranger"
                )
        );

        for (String category : userGreenCategories) {
            suggestions.addAll(categoryJobTitles.getOrDefault(category, new ArrayList<>()));
        }

        return suggestions.stream().distinct().limit(10).collect(Collectors.toList());
    }
    
}
