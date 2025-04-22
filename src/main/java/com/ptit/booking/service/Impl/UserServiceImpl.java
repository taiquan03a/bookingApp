package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.user.Information;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.model.User;
import com.ptit.booking.repository.UserRepository;
import com.ptit.booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<?> getInformation(Principal principal) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(ErrorMessage.PLEASE_LOGIN)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        Information information = Information.builder()
                .avatar(user.getAvatar())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.INFO_PAGE)
                .data(information)
                .build());
    }
}

