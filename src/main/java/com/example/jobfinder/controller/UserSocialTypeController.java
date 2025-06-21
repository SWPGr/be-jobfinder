package com.example.jobfinder.controller;

import com.example.jobfinder.dto.user_social_type.UserSocialTypeRequest;
import com.example.jobfinder.dto.user_social_type.UserSocialTypeResponse;
import com.example.jobfinder.service.UserSocialTypeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-social-links")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSocialTypeController {

    UserSocialTypeService userSocialTypeService;

    @PostMapping
    public ResponseEntity<UserSocialTypeResponse> createSocialLink(@RequestBody @Valid UserSocialTypeRequest request) {
        UserSocialTypeResponse response = userSocialTypeService.createUserSocialLink(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserSocialTypeResponse>> getMySocialLinks() {
        List<UserSocialTypeResponse> response = userSocialTypeService.getMySocialLinks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserSocialTypeResponse> getSocialLinkById(@PathVariable Long id) {
        UserSocialTypeResponse response = userSocialTypeService.getSocialLinkById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserSocialTypeResponse> updateSocialLink(@PathVariable Long id,
                                                                   @RequestBody @Valid UserSocialTypeRequest request) {
        UserSocialTypeResponse response = userSocialTypeService.updateUserSocialLink(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSocialLink(@PathVariable Long id) {
        userSocialTypeService.deleteUserSocialLink(id);
        return ResponseEntity.noContent().build();
    }
}