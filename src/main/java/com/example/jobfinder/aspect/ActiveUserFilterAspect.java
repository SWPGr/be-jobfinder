package com.example.jobfinder.aspect;

import com.example.jobfinder.service.FilterService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ActiveUserFilterAspect {

    private final FilterService filterService;

    public ActiveUserFilterAspect(FilterService filterService) {
        this.filterService = filterService;
    }

    // Áp dụng Aspect cho tất cả các Controller loại bỏ authController để đăng nhập ban đầu lấy trường activeUserFilter
    @Around("execution(* com.example.jobfinder.controller.*.*(..)) && !execution(* com.example.jobfinder.controller.AuthController.*(..)) && !execution(* com.example.jobfinder.controller.AdminController.*(..))")
    public Object applyFilters(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        try {
            filterService.enableUserAndRelatedFilters();
            result = joinPoint.proceed();
        } finally {
            filterService.disableUserAndRelatedFilters();
        }
        return result;
    }
}