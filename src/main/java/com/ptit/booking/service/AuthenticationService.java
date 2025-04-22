package com.ptit.booking.service;


import com.google.firebase.auth.FirebaseAuthException;
import com.ptit.booking.dto.auth.AuthenticationRequest;
import com.ptit.booking.dto.auth.FirebaseTokenId;
import com.ptit.booking.dto.auth.RegisterRequest;
import com.ptit.booking.dto.auth.UpdatePasswordRequest;
import com.ptit.booking.dto.user.UpdateProfileRequest;
import com.ptit.booking.model.User;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.security.Principal;

public interface AuthenticationService {
    ResponseEntity<?> updateProfile(UpdateProfileRequest updateProfileRequest,Principal principal);
    ResponseEntity<?> authenticate(AuthenticationRequest request, HttpServletRequest httpServletRequest, HttpServletResponse response, Authentication authentication) throws java.io.IOException;
    ResponseEntity<?> register(RegisterRequest request) throws FirebaseAuthException;
    ResponseEntity<?> updatePassword(UpdatePasswordRequest updatePasswordRequest, Principal connectedUser);
    void revokeAllUserTokens(User user);
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, java.io.IOException;
    ResponseEntity<?> authFirebase(HttpServletRequest httpServletRequest, HttpServletResponse response, FirebaseTokenId tokenId);
}