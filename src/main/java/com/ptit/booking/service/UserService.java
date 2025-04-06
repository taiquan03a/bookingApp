package com.ptit.booking.service;

import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface UserService {
    ResponseEntity<?> getInformation(Principal principal);
}
