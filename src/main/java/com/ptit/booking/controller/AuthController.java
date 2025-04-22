package com.ptit.booking.controller;


import com.google.firebase.auth.FirebaseAuthException;
import com.ptit.booking.dto.auth.AuthenticationRequest;
import com.ptit.booking.dto.auth.FirebaseTokenId;
import com.ptit.booking.dto.auth.RegisterRequest;
import com.ptit.booking.dto.auth.UpdatePasswordRequest;
import com.ptit.booking.exception.InputFieldException;
import com.ptit.booking.security.LogoutService;
import com.ptit.booking.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

import static com.ptit.booking.constants.ErrorMessage.INCORRECT_PASSWORD_CONFIRMATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth API",description = "api auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final LogoutService logoutService;

    @PostMapping("/firebase")
    public ResponseEntity<?> authenticateWithFirebase(
            @RequestBody FirebaseTokenId tokenId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse response
    ) {
        return authenticationService.authFirebase(httpServletRequest,response,tokenId);
    }

    @GetMapping("")
    @Operation(summary = "check",description = "check description")
    public ResponseEntity<?> authenticate() {
        return ResponseEntity.ok("You are authenticated");
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) throws FirebaseAuthException {
        if (bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).body(new InputFieldException(bindingResult).getMessage());
        }
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody AuthenticationRequest authenticationRequest,
                                   BindingResult bindingResult,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Authentication authentication
    ) throws IOException {
        if (bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).body(new InputFieldException(bindingResult).getMessage());
        }
        return authenticationService.authenticate(authenticationRequest, request, response, authentication);
    }


    @PostMapping("/password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest updatePasswordRequest, Principal connectedUser, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new InputFieldException(bindingResult).getMessage());
        }
        if (!updatePasswordRequest.getNewPassword().equals(updatePasswordRequest.getNewPasswordConfirm())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(INCORRECT_PASSWORD_CONFIRMATION);
        }
        return authenticationService.updatePassword(updatePasswordRequest, connectedUser);
    }

    @DeleteMapping("/logout")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMINISTRATOR')")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        logoutService.logout(request, response, authentication);
        return ResponseEntity.ok("Log out successfully!");
    }
    @PostMapping("/refresh")
    public void refresh(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        authenticationService.refreshToken(request, response);
    }
}

