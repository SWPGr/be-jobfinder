package com.example.jobfinder.controller.green;

import com.example.jobfinder.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GreenJobScheduler {

    private final RecommendationService recommendationService;

    // Run green job recommendations mỗi ngày lúc 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyGreenJobRecommendations() {
        recommendationService.generateGreenJobRecommendations();
    }

    // Run green job recommendations cho Green Week (mỗi 4 tiếng)
    @Scheduled(cron = "0 0 */4 * * ?")
    public void generateGreenWeekRecommendations() {
        recommendationService.generateGreenJobRecommendations();
    }
}
