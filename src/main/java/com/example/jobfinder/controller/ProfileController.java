package com.example.jobfinder.controller;

import com.example.jobfinder.dto.auth.ProfileRequest;
import com.example.jobfinder.dto.auth.ProfileResponse;
import com.example.jobfinder.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile( @ModelAttribute ProfileRequest Request) throws Exception {
        ProfileResponse response = profileService.updateProfile(Request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ProfileResponse>> getCurrentUserProfile() throws Exception {
        List<ProfileResponse> profile = profileService.listCurrentUserProfiles();
        return ResponseEntity.ok(profile);
    }
}
