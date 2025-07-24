package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.simple.SimpleNameResponse; // THÊM IMPORT NÀY
import com.example.jobfinder.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.example.jobfinder.model.Role; // THÊM IMPORT NÀY
import com.example.jobfinder.model.SubscriptionPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class}) // THÊM uses = {RoleMapper.class}
public interface SubscriptionPlanMapper {
    SubscriptionPlanMapper INSTANCE = Mappers.getMapper(SubscriptionPlanMapper.class);

    @Mapping(source = "role", target = "role") // Ánh xạ từ Role entity sang SimpleNameResponse
    SubscriptionPlanResponse toSubscriptionPlanResponse(SubscriptionPlan subscriptionPlan);

    @Mapping(target = "name", source = "name")
    SimpleNameResponse toSimpleNameResponse(Role role);

    List<SubscriptionPlanResponse> toSubscriptionPlanResponseList(List<SubscriptionPlan> subscriptionPlans);
}