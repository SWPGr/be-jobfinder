package com.example.jobfinder.service;

import com.example.jobfinder.dto.simple.SimpleNameCreationRequest;
import com.example.jobfinder.dto.simple.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.SocialTypeMapper;
import com.example.jobfinder.model.SocialType;
import com.example.jobfinder.repository.SocialTypeRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocialTypeService extends BaseNameService<SocialType> {

    SocialTypeMapper socialTypeMapper;

    public SocialTypeService(SocialTypeRepository socialTypeRepository, SocialTypeMapper socialTypeMapper) {
        super(socialTypeRepository);
        this.socialTypeMapper = socialTypeMapper;
    }


    @Override
    protected SocialType createEntity(SimpleNameCreationRequest request) {
        return socialTypeMapper.toSocialType(request);
    }

    @Override
    protected void updateEntity(SocialType entity, SimpleNameUpdateRequest request) {
        socialTypeMapper.updateSocialType(entity, request);
    }

    @Override
    protected SimpleNameResponse toResponse(SocialType entity) {
        return socialTypeMapper.toSimpleNameResponse(entity);
    }

    @Override
    protected ErrorCode getExistedErrorCode() {
        return ErrorCode.SOCIAL_TYPE_ALREADY_EXISTS;
    }

    @Override
    protected ErrorCode getNotFoundErrorCode() {
        return ErrorCode.SOCIAL_TYPE_NOT_FOUND;
    }

    @Override
    protected String getEntityNameForLog() {
        return "Social Type";
    }

}