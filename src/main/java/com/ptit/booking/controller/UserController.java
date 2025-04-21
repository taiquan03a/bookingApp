package com.ptit.booking.controller;

import com.ptit.booking.dto.user.UpdateProfileRequest;
import com.ptit.booking.service.AuthenticationService;
import com.ptit.booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
public class UserController {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    @GetMapping("info")
    public ResponseEntity<?> getInformation(Principal principal){
        return userService.getInformation(principal);
    }

    @PostMapping("update")
    public ResponseEntity<?> updateInformation(Principal principal, @ModelAttribute UpdateProfileRequest user){
        return authenticationService.updateProfile(user,principal);
    }
}
