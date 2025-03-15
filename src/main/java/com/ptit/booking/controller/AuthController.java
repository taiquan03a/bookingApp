package com.ptit.booking.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/firebase")
    public ResponseEntity<?> authenticateWithFirebase(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("idToken");
            System.out.println("idToken-> "+ idToken);
            // Xác thực token từ Firebase
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            // Tạo JWT token cho backend
            String jwtToken = "Bearer " + uid;

            Map<String, String> response = new HashMap<>();
            response.put("jwtToken", jwtToken);
            System.out.println("check");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid Firebase ID Token");
        }
    }
    @GetMapping("")
    public ResponseEntity<?> authenticate() {
        return ResponseEntity.ok("You are authenticated");
    }
}

