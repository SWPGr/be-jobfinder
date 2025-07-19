package com.example.jobfinder.service;

import com.example.jobfinder.dto.green.CompanyGreenMetrics;
import com.example.jobfinder.dto.green.GreenCompanyAnalysis;
import com.example.jobfinder.dto.green.CompanyGreenScore;
import com.example.jobfinder.model.CompanyAnalysis;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.repository.CompanyAnalysisRepository;
import com.example.jobfinder.repository.UserDetailsRepository;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GreenCompanyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(GreenCompanyDetectionService.class);

    private final StanfordCoreNLP pipeline;
    private final CompanyAnalysisRepository companyAnalysisRepository;
    private final UserDetailsRepository userDetailsRepository;

    // Enhanced Green Market Positioning Keywords
    private static final Map<String, Set<String>> GREEN_MARKET_POSITIONING_KEYWORDS = Map.of(
            "SUSTAINABLE_LEADERSHIP", Set.of(
                    "market leader in sustainability", "green innovation leader", "environmental pioneer",
                    "sustainability champion", "clean technology leader", "ESG leader",
                    "carbon neutral", "net zero", "climate leader", "green transformation",
                    "sustainable solutions provider", "environmental excellence", "green business model",
                    "sustainable development", "eco-innovation", "climate positive", "regenerative business"
            ),
            "GREEN_PRODUCTS_SERVICES", Set.of(
                    "eco-friendly products", "green services", "sustainable offerings",
                    "environmentally responsible", "clean technology solutions",
                    "renewable energy solutions", "green infrastructure", "sustainable supply chain",
                    "circular business model", "green product portfolio", "sustainable manufacturing",
                    "clean production", "green technology", "eco-design", "sustainable innovation"
            ),
            "ENVIRONMENTAL_IMPACT", Set.of(
                    "positive environmental impact", "environmental stewardship", "ecological footprint",
                    "environmental responsibility", "sustainable practices", "green operations",
                    "environmental compliance", "pollution reduction", "resource efficiency",
                    "carbon footprint reduction", "environmental restoration", "ecosystem protection",
                    "biodiversity conservation", "climate impact", "environmental preservation"
            ),
            "CIRCULAR_ECONOMY", Set.of(
                    "circular economy", "waste to value", "zero waste", "material recovery",
                    "product lifecycle", "cradle to cradle", "regenerative design", "upcycling",
                    "resource circularity", "closed loop", "waste minimization", "material efficiency",
                    "industrial symbiosis", "sharing economy", "product as service"
            )
    );

    // Enhanced CSR & Sustainability Keywords
    private static final Map<String, Set<String>> CSR_SUSTAINABILITY_KEYWORDS = Map.of(
            "ENVIRONMENTAL_INITIATIVES", Set.of(
                    "carbon reduction", "emission reduction", "renewable energy adoption",
                    "waste reduction programs", "recycling initiatives", "energy efficiency",
                    "water conservation", "sustainable sourcing", "green building certification",
                    "environmental management system", "climate action", "biodiversity protection",
                    "carbon offsetting", "clean energy transition", "environmental monitoring",
                    "pollution prevention", "resource conservation", "ecosystem restoration"
            ),
            "SOCIAL_RESPONSIBILITY", Set.of(
                    "community engagement", "social impact", "stakeholder engagement",
                    "employee wellbeing", "diversity and inclusion", "fair trade",
                    "ethical sourcing", "community development", "social investment",
                    "responsible business practices", "sustainable development goals",
                    "social equity", "community partnership", "local sourcing", "fair labor practices"
            ),
            "GOVERNANCE_SUSTAINABILITY", Set.of(
                    "ESG reporting", "sustainability reporting", "transparent governance",
                    "ethical business practices", "sustainable governance", "accountability",
                    "stakeholder capitalism", "long-term value creation", "responsible leadership",
                    "sustainability metrics", "impact measurement", "ESG integration",
                    "sustainable finance", "green bonds", "impact investing", "B-Corp certification"
            ),
            "GREEN_INNOVATION", Set.of(
                    "green R&D", "sustainable innovation", "clean technology development",
                    "environmental technology", "green patents", "eco-innovation labs",
                    "sustainable product development", "green digitalization", "clean tech startups",
                    "environmental solutions", "climate technology", "green investments"
            )
    );

    // Company Size & Industry Multipliers
    private static final Map<String, Double> INDUSTRY_GREEN_MULTIPLIERS = Map.of(
            "RENEWABLE_ENERGY", 1.5,
            "ENVIRONMENTAL_SERVICES", 1.4,
            "SUSTAINABLE_AGRICULTURE", 1.3,
            "GREEN_TECHNOLOGY", 1.3,
            "CLEAN_TRANSPORTATION", 1.2,
            "SUSTAINABLE_FINANCE", 1.1,
            "TRADITIONAL_ENERGY", 0.8,
            "HEAVY_MANUFACTURING", 0.7,
            "MINING", 0.6
    );

    /**
     * Main method - Analyze company green credentials
     */
    public GreenCompanyAnalysis analyzeGreenCompany(Long employerId) {
        if (employerId == null) {
            return createDefaultGreenCompanyAnalysis();
        }

        try {
            // 1. Get company data
            CompanyAnalysis companyAnalysis = getCompanyAnalysisByEmployerId(employerId);
            UserDetail userDetail = getUserDetailByEmployerId(employerId);

            if (companyAnalysis == null && userDetail == null) {
                log.warn("No company data found for employer: {}", employerId);
                return createDefaultGreenCompanyAnalysis();
            }

            // 2. Analyze company green data
            CompanyGreenMetrics greenMetrics = analyzeCompanyGreenData(companyAnalysis, userDetail);

            // 3. Calculate overall green score
            CompanyGreenScore greenScore = calculateOverallGreenScore(greenMetrics, userDetail);

            // 4. Determine green company status
            boolean isGreenCompany = greenScore.getOverallScore() >= 0.4; // Threshold for green company

            // 5. Generate recommendations
            List<String> recommendations = generateGreenRecommendations(greenMetrics, greenScore);

            GreenCompanyAnalysis analysis = GreenCompanyAnalysis.builder()
                    .employerId(employerId)
                    .isGreenCompany(isGreenCompany)
                    .overallGreenScore(greenScore.getOverallScore())
                    .marketPositioningScore(greenScore.getMarketPositioningScore())
                    .csrSustainabilityScore(greenScore.getCsrSustainabilityScore())
                    .greenCategories(greenMetrics.getAllCategories())
                    .greenKeywords(greenMetrics.getAllKeywords())
                    .primaryGreenCategory(determinePrimaryGreenCategory(greenMetrics))
                    .greenStrengths(identifyGreenStrengths(greenMetrics))
                    .improvementAreas(identifyImprovementAreas(greenMetrics))
                    .recommendations(recommendations)
                    .industryBenchmark(calculateIndustryBenchmark(userDetail))
                    .certificationLevel(determineCertificationLevel(greenScore.getOverallScore()))
                    .build();

            log.debug("Green company analysis completed for employer: {} with score: {}",
                    employerId, greenScore.getOverallScore());

            return analysis;

        } catch (Exception e) {
            log.error("Error in green company analysis for employer: {}", employerId, e);
            return createDefaultGreenCompanyAnalysis();
        }
    }

    /**
     * Analyze Company Green Data từ CompanyAnalysis và UserDetail
     */
    private CompanyGreenMetrics analyzeCompanyGreenData(CompanyAnalysis companyAnalysis, UserDetail userDetail) {
        CompanyGreenMetrics metrics = new CompanyGreenMetrics();

        // Analyze CompanyAnalysis data
        if (companyAnalysis != null) {
            analyzeMarketPositioning(companyAnalysis.getMarketPositioningSummary(), metrics);
            analyzeCSRSustainability(companyAnalysis.getCsrAndSustainabilityInitiatives(), metrics);
            analyzeAdditionalFields(companyAnalysis, metrics);
        }

        // Analyze UserDetail data (company profile)
        if (userDetail != null) {
            analyzeCompanyProfile(userDetail, metrics);
        }

        return metrics;
    }

    /**
     * Analyze Market Positioning Summary
     */
    private void analyzeMarketPositioning(String marketPositioning, CompanyGreenMetrics metrics) {
        if (marketPositioning == null || marketPositioning.trim().isEmpty()) {
            return;
        }

        String normalizedText = normalizeText(marketPositioning);

        List<String> keywords = extractKeywordsFromCategories(normalizedText, GREEN_MARKET_POSITIONING_KEYWORDS);
        List<String> categories = detectCategoriesFromKeywords(normalizedText, GREEN_MARKET_POSITIONING_KEYWORDS);
        double score = calculateCategoryScore(normalizedText, GREEN_MARKET_POSITIONING_KEYWORDS);

        metrics.setMarketKeywords(keywords);
        metrics.setMarketCategories(categories);
        metrics.setMarketPositioningScore(score);
    }

    /**
     * Analyze CSR & Sustainability Initiatives
     */
    private void analyzeCSRSustainability(String csrSustainability, CompanyGreenMetrics metrics) {
        if (csrSustainability == null || csrSustainability.trim().isEmpty()) {
            return;
        }

        String normalizedText = normalizeText(csrSustainability);

        List<String> keywords = extractKeywordsFromCategories(normalizedText, CSR_SUSTAINABILITY_KEYWORDS);
        List<String> categories = detectCategoriesFromKeywords(normalizedText, CSR_SUSTAINABILITY_KEYWORDS);
        double score = calculateCategoryScore(normalizedText, CSR_SUSTAINABILITY_KEYWORDS);

        metrics.setCsrKeywords(keywords);
        metrics.setCsrCategories(categories);
        metrics.setCsrSustainabilityScore(score);
    }

    /**
     * Analyze additional CompanyAnalysis fields
     */
    private void analyzeAdditionalFields(CompanyAnalysis companyAnalysis, CompanyGreenMetrics metrics) {
        // Analyze other relevant fields
        analyzeTextField(companyAnalysis.getCoreCompetencies(), metrics, "COMPETENCIES");
        analyzeTextField(companyAnalysis.getGrowthPotentialSummary(), metrics, "GROWTH");
        analyzeTextField(companyAnalysis.getTalentDevelopmentFocus(), metrics, "TALENT");
        analyzeTextField(companyAnalysis.getCommunityOutreachPrograms(), metrics, "COMMUNITY");
    }

    /**
     * Analyze company profile từ UserDetail
     */
    private void analyzeCompanyProfile(UserDetail userDetail, CompanyGreenMetrics metrics) {
        // Analyze company name
        if (userDetail.getCompanyName() != null) {
            boolean hasGreenName = containsGreenCompanyIndicators(userDetail.getCompanyName().toLowerCase());
            metrics.setHasGreenCompanyName(hasGreenName);
        }

        // Analyze company description
        if (userDetail.getDescription() != null) {
            String normalizedDesc = normalizeText(userDetail.getDescription());
            List<String> descKeywords = extractGreenKeywordsFromText(normalizedDesc);
            metrics.getProfileKeywords().addAll(descKeywords);
        }

        // Analyze website (could check for green/sustainability pages)
        if (userDetail.getWebsite() != null) {
            boolean hasGreenWebsite = containsGreenWebsiteIndicators(userDetail.getWebsite());
            metrics.setHasGreenWebsite(hasGreenWebsite);
        }
    }

    /**
     * Calculate overall green score
     */
    private CompanyGreenScore calculateOverallGreenScore(CompanyGreenMetrics metrics, UserDetail userDetail) {
        double marketScore = metrics.getMarketPositioningScore();
        double csrScore = metrics.getCsrSustainabilityScore();

        // Profile bonus
        double profileBonus = 0.0;
        if (metrics.isHasGreenCompanyName()) profileBonus += 0.1;
        if (metrics.isHasGreenWebsite()) profileBonus += 0.05;
        if (!metrics.getProfileKeywords().isEmpty())
            profileBonus += Math.min(metrics.getProfileKeywords().size() * 0.02, 0.1);

        // Industry multiplier
        double industryMultiplier = getIndustryMultiplier(userDetail);

        // Calculate weighted score
        double baseScore = (marketScore * 0.6) + (csrScore * 0.4);
        double finalScore = Math.min((baseScore + profileBonus) * industryMultiplier, 1.0);

        return CompanyGreenScore.builder()
                .marketPositioningScore(marketScore)
                .csrSustainabilityScore(csrScore)
                .profileBonus(profileBonus)
                .industryMultiplier(industryMultiplier)
                .overallScore(finalScore)
                .build();
    }

    /**
     * Get industry multiplier based on company description/industry
     */
    private double getIndustryMultiplier(UserDetail userDetail) {
        if (userDetail == null || userDetail.getDescription() == null) {
            return 1.0;
        }

        String description = userDetail.getDescription().toLowerCase();

        for (Map.Entry<String, Double> entry : INDUSTRY_GREEN_MULTIPLIERS.entrySet()) {
            String industry = entry.getKey().toLowerCase().replace("_", " ");
            if (description.contains(industry)) {
                return entry.getValue();
            }
        }

        return 1.0; // Default multiplier
    }

    /**
     * Generate green recommendations based on analysis
     */
    private List<String> generateGreenRecommendations(CompanyGreenMetrics metrics, CompanyGreenScore score) {
        List<String> recommendations = new ArrayList<>();

        // Market positioning recommendations
        if (score.getMarketPositioningScore() < 0.3) {
            recommendations.add("Enhance market positioning by highlighting sustainability initiatives");
            recommendations.add("Develop green product/service offerings");
            recommendations.add("Communicate environmental leadership more effectively");
        }

        // CSR recommendations
        if (score.getCsrSustainabilityScore() < 0.4) {
            recommendations.add("Implement comprehensive CSR and sustainability programs");
            recommendations.add("Establish measurable environmental targets");
            recommendations.add("Engage in community environmental initiatives");
        }

        // Profile recommendations
        if (!metrics.isHasGreenCompanyName() && !metrics.isHasGreenWebsite()) {
            recommendations.add("Consider rebranding to reflect environmental commitment");
            recommendations.add("Create dedicated sustainability section on company website");
        }

        // General recommendations based on score
        if (score.getOverallScore() < 0.6) {
            recommendations.add("Obtain green certifications (ISO 14001, B-Corp, etc.)");
            recommendations.add("Implement comprehensive environmental management system");
            recommendations.add("Publish annual sustainability reports");
        }

        return recommendations;
    }

    // Helper methods
    private List<String> extractKeywordsFromCategories(String text, Map<String, Set<String>> categories) {
        List<String> keywords = new ArrayList<>();
        String lowerText = text.toLowerCase();

        for (Set<String> categoryKeywords : categories.values()) {
            for (String keyword : categoryKeywords) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    keywords.add(keyword);
                }
            }
        }

        return keywords.stream().distinct().collect(Collectors.toList());
    }

    private List<String> detectCategoriesFromKeywords(String text, Map<String, Set<String>> categories) {
        List<String> detectedCategories = new ArrayList<>();
        String lowerText = text.toLowerCase();

        for (Map.Entry<String, Set<String>> entry : categories.entrySet()) {
            String category = entry.getKey();
            Set<String> keywords = entry.getValue();

            long matchCount = keywords.stream()
                    .filter(keyword -> lowerText.contains(keyword.toLowerCase()))
                    .count();

            if (matchCount > 0) {
                detectedCategories.add(category);
            }
        }

        return detectedCategories;
    }

    private double calculateCategoryScore(String text, Map<String, Set<String>> categories) {
        double score = 0.0;
        String lowerText = text.toLowerCase();

        // Keyword density score (60%)
        long totalKeywords = categories.values().stream()
                .flatMap(Set::stream)
                .filter(keyword -> lowerText.contains(keyword.toLowerCase()))
                .count();

        double keywordScore = Math.min(totalKeywords * 0.08, 0.6);
        score += keywordScore;

        // Category diversity score (40%)
        long categoryCount = categories.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(keyword -> lowerText.contains(keyword.toLowerCase())))
                .count();

        double categoryScore = Math.min(categoryCount * 0.12, 0.4);
        score += categoryScore;

        return Math.min(score, 1.0);
    }

    private void analyzeTextField(String text, CompanyGreenMetrics metrics, String context) {
        if (text == null || text.trim().isEmpty()) return;

        String normalizedText = normalizeText(text);
        List<String> keywords = extractGreenKeywordsFromText(normalizedText);

        metrics.getAdditionalKeywords().addAll(keywords);
    }

    private List<String> extractGreenKeywordsFromText(String text) {
        List<String> keywords = new ArrayList<>();

        // Combine all green keywords from different categories
        Set<String> allGreenKeywords = new HashSet<>();
        GREEN_MARKET_POSITIONING_KEYWORDS.values().forEach(allGreenKeywords::addAll);
        CSR_SUSTAINABILITY_KEYWORDS.values().forEach(allGreenKeywords::addAll);

        String lowerText = text.toLowerCase();
        for (String keyword : allGreenKeywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                keywords.add(keyword);
            }
        }

        return keywords.stream().distinct().collect(Collectors.toList());
    }

    private boolean containsGreenCompanyIndicators(String companyName) {
        String[] greenIndicators = {
                "green", "eco", "sustainable", "renewable", "clean", "environment",
                "solar", "wind", "energy", "organic", "bio", "recycle", "carbon",
                "climate", "earth", "nature", "planet", "pure", "fresh"
        };

        for (String indicator : greenIndicators) {
            if (companyName.contains(indicator)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsGreenWebsiteIndicators(String website) {
        String lowerWebsite = website.toLowerCase();
        return lowerWebsite.contains("sustainability") ||
                lowerWebsite.contains("green") ||
                lowerWebsite.contains("environment") ||
                lowerWebsite.contains("csr");
    }

    private String determinePrimaryGreenCategory(CompanyGreenMetrics metrics) {
        Map<String, Integer> categoryCount = new HashMap<>();

        metrics.getMarketCategories().forEach(cat ->
                categoryCount.put(cat, categoryCount.getOrDefault(cat, 0) + 2)); // Market categories weighted more
        metrics.getCsrCategories().forEach(cat ->
                categoryCount.put(cat, categoryCount.getOrDefault(cat, 0) + 1));

        return categoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("GENERAL_SUSTAINABILITY");
    }

    private List<String> identifyGreenStrengths(CompanyGreenMetrics metrics) {
        List<String> strengths = new ArrayList<>();

        if (metrics.getMarketPositioningScore() > 0.6) {
            strengths.add("Strong sustainable market positioning");
        }
        if (metrics.getCsrSustainabilityScore() > 0.6) {
            strengths.add("Comprehensive CSR and sustainability initiatives");
        }
        if (metrics.isHasGreenCompanyName()) {
            strengths.add("Green-focused company branding");
        }
        if (metrics.getAllKeywords().size() > 10) {
            strengths.add("Extensive green vocabulary and messaging");
        }

        return strengths;
    }

    private List<String> identifyImprovementAreas(CompanyGreenMetrics metrics) {
        List<String> improvements = new ArrayList<>();

        if (metrics.getMarketPositioningScore() < 0.4) {
            improvements.add("Market positioning and green messaging");
        }
        if (metrics.getCsrSustainabilityScore() < 0.4) {
            improvements.add("CSR and sustainability program development");
        }
        if (!metrics.isHasGreenCompanyName() && !metrics.isHasGreenWebsite()) {
            improvements.add("Green branding and online presence");
        }
        if (metrics.getAllCategories().size() < 2) {
            improvements.add("Diversification of green initiatives");
        }

        return improvements;
    }

    private double calculateIndustryBenchmark(UserDetail userDetail) {
        // Simplified industry benchmark calculation
        return 0.5; // Default benchmark
    }

    private String determineCertificationLevel(double score) {
        if (score >= 0.8) return "GREEN_LEADER";
        if (score >= 0.6) return "GREEN_CERTIFIED";
        if (score >= 0.4) return "GREEN_EMERGING";
        if (score >= 0.2) return "GREEN_BASIC";
        return "GREEN_STARTER";
    }

    private CompanyAnalysis getCompanyAnalysisByEmployerId(Long employerId) {
        try {
            return companyAnalysisRepository.findByUserDetailUserId(employerId)
                    .orElse(null);
        } catch (Exception e) {
            log.debug("Could not find company analysis for employer: {}", employerId);
            return null;
        }
    }

    private UserDetail getUserDetailByEmployerId(Long employerId) {
        try {
            return userDetailsRepository.findByUserId(employerId)
                    .orElse(null);
        } catch (Exception e) {
            log.debug("Could not find user detail for employer: {}", employerId);
            return null;
        }
    }

    private String normalizeText(String text) {
        if (text == null) return "";

        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

        return document.tokens().stream()
                .map(token -> token.lemma().toLowerCase())
                .filter(lemma -> lemma.length() > 2)
                .collect(Collectors.joining(" "));
    }

    private GreenCompanyAnalysis createDefaultGreenCompanyAnalysis() {
        return GreenCompanyAnalysis.builder()
                .isGreenCompany(false)
                .overallGreenScore(0.0)
                .marketPositioningScore(0.0)
                .csrSustainabilityScore(0.0)
                .greenCategories(new ArrayList<>())
                .greenKeywords(new ArrayList<>())
                .primaryGreenCategory("UNKNOWN")
                .greenStrengths(new ArrayList<>())
                .improvementAreas(List.of("Complete company profile", "Add sustainability information"))
                .recommendations(List.of("Complete company analysis to get green assessment"))
                .industryBenchmark(0.0)
                .certificationLevel("UNASSESSED")
                .build();
    }
}