package com.ptit.booking.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.exceptions.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.*;
import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.auth.*;
import com.ptit.booking.dto.user.UpdateProfileRequest;
import com.ptit.booking.enums.EnumRole;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.model.Role;
import com.ptit.booking.model.Token;
import com.ptit.booking.model.User;
import com.ptit.booking.repository.RoleRepository;
import com.ptit.booking.repository.TokenRepository;
import com.ptit.booking.repository.UserRepository;
import com.ptit.booking.security.JwtService;
import com.ptit.booking.service.AuthenticationService;
import com.ptit.booking.service.CloudinaryService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;
import java.util.HashSet;
import java.util.Random;

import static com.ptit.booking.constants.ErrorMessage.*;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinary;


    @Override
    public ResponseEntity<?> register(RegisterRequest request) throws FirebaseAuthException {

        var existedUser = userRepository.findByEmail(request.getEmail());
        if (existedUser.isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.builder()
                    .statusCode(409)
                    .message(String.valueOf(HttpStatus.CONFLICT))
                    .description(EMAIL_IN_USE)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());

        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.getEmail())
                .setPassword(request.getPassword());
        UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
        var role = new HashSet<Role>();
        role.add(roleRepository.findRoleByRole(EnumRole.ROLE_USER.name()));
        var user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUid(userRecord.getUid());
        user.setPhone(request.getPhoneNumber());
        user.setRoles(role);
        user.setActive(true);
        user.setEmailActive(true);
        user.setCreatedAt(new Date(System.currentTimeMillis()));
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(200)
                .message("Taọ tài khoản thành công")
                .description("Successfully")
                .timestamp(new Date(System.currentTimeMillis()))
                .build());
    }

    @Override
    public ResponseEntity<?> updatePassword(UpdatePasswordRequest updatePasswordRequest, Principal connectedUser) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // check if the current password is correct
        if (!passwordEncoder.matches(updatePasswordRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                    .statusCode(400)
                    .message(String.valueOf(HttpStatus.FORBIDDEN))
                    .description(INCORRECT_PASSWORD)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        // check if new passwords are the same current password
        if (updatePasswordRequest.getNewPassword().equals(updatePasswordRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .statusCode(402)
                    .message("SAME_CURRENT_PASSWORD")
                    .description(NEW_PASSWORD_IS_SAME_CURRENT_PASSWORD)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }

        user.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .statusCode(200)
                .message(String.valueOf(HttpStatus.OK))
                .description("Mật khẩu của bạn đã cập nhập thành công.")
                .timestamp(new Date(System.currentTimeMillis()))
                .build());
    }

    @Override
    public ResponseEntity<?> updateProfile(UpdateProfileRequest request,Principal principal) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(ErrorMessage.PLEASE_LOGIN)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        if(request.getImage() != null){
            String url = cloudinary.uploadFile(request.getImage(),"user");
            user.setAvatar(url);
        }

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .statusCode(200)
                .message(String.valueOf(HttpStatus.OK))
                .description("Cập nhật thông tin thành công.")
                .timestamp(new Date(System.currentTimeMillis()))
                .build());
    }

    @Override
    public ResponseEntity<?> authenticate(AuthenticationRequest request, HttpServletRequest httpServletRequest, HttpServletResponse response, Authentication authentication) throws IOException {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                        .statusCode(403)
                        .message(String.valueOf(HttpStatus.FORBIDDEN))
                        .description(INACTIVE)
                        .timestamp(new Date(System.currentTimeMillis()))
                        .build());
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(String.valueOf(HttpStatus.BAD_REQUEST))
                        .description(INCORRECT_PASSWORD)
                        .timestamp(new Date(System.currentTimeMillis()))
                        .build());
            } else {
                var jwtToken = jwtService.generateToken(user, user);
                var refreshToken = jwtService.generateRefreshToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, jwtToken);
                System.out.println("đã vào dc");
                Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
                refreshTokenCookie.setHttpOnly(true);
                response.addCookie(refreshTokenCookie);
                httpServletRequest.getCookies();
                System.out.println("đã vào dc response add cookie");
                List<String> roles = new ArrayList<>();
                for(Role role : user.getRoles()) {
                    roles.add(role.getRole());
                }
//                return ResponseEntity.ok(AuthenticationResponse.builder()
//                                .accessToken(jwtToken)
//                                .roles(roles)
//                                .build());
                AuthenticationResponse data =  AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .roles(roles)
                        .build();
                return ResponseEntity.ok(ApiResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Thành công")
                        .description("Successfully authenticated")
                        .data(data)
                        .build());
            }

        } else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .message(String.valueOf(HttpStatus.NOT_FOUND))
                    .description(EMAIL_NOT_FOUND)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());

    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        Cookie[] Cookies = request.getCookies();
        String cookie_ = null;
        String userEmail = null;
        if (Cookies.length > 0){
            for (Cookie cookie : Cookies){
                System.out.println(cookie.getName());

                if (cookie.getName().equals("refreshToken")){
                    cookie_ = cookie.getValue();
                    System.out.println("name: " + cookie.getName());
                    System.out.println("value: " + cookie.getValue());
                    System.out.println("attribute: " + cookie.getAttribute("refreshToken"));
                    System.out.println("secure: " + cookie.getSecure());
                    System.out.println(cookie_);
                }
            }
        }
        else {
            var authResponse = ErrorResponse.builder()
                    .statusCode(400)
                    .message(String.valueOf(HttpStatus.NOT_FOUND))
                    .description("Did not found any cookies!")
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build();
            new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
        }

        userEmail = jwtService.extractEmail(cookie_);
        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isFreshTokenValid(cookie_, user)) {
                var accessToken = jwtService.generateToken(user, user);
                var refreshToken = jwtService.generateRefreshToken(user);
                Cookie newCookie = new Cookie("refreshToken", refreshToken);
                newCookie.setHttpOnly(true);
                response.addCookie(newCookie);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    @Override
    public ResponseEntity<?> authFirebase(
            HttpServletRequest httpServletRequest,
            HttpServletResponse response,
            FirebaseTokenId tokenId
    ){
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(tokenId.getTokenId());
            String userEmail = Optional.ofNullable(decodedToken.getEmail())
                    .map(Object::toString)
                    .orElse("");
            String phoneNumber = Optional.ofNullable(decodedToken.getClaims().get("phone_number"))
                    .map(Object::toString)
                    .orElse("");
            String uid = decodedToken.getUid();
            if (userEmail.isEmpty() && phoneNumber.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder()
                        .statusCode(ErrorCode.EMAIL_OR_PHONE_NOT_FOUND.getCode())
                        .message(ErrorCode.EMAIL_OR_PHONE_NOT_FOUND.getMessage())
                        .description(ErrorCode.EMAIL_OR_PHONE_NOT_FOUND.getMessage())
                        .timestamp(new Date(System.currentTimeMillis()))
                        .build());
            var exitsUser = userRepository.findByUid(uid);
            User user = new User();
            if (exitsUser.isEmpty()) {
                var role = new HashSet<Role>();
                role.add(roleRepository.findRoleByRole(EnumRole.ROLE_USER.name()));
                user.setEmail(userEmail);
                user.setPhone(phoneNumber);
                user.setRoles(role);
                user.setActive(true);
                user.setEmailActive(!userEmail.isEmpty());
                user.setPhoneActive(!phoneNumber.isEmpty());
                user.setCreatedAt(new Date(System.currentTimeMillis()));
                user.setPassword(uid);
                user.setUid(uid);
                userRepository.save(user);
            } else {
                user = exitsUser.get();
            }
            var jwtToken = jwtService.generateToken(user, user);
            var refreshToken = jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);
            System.out.println("đã vào dc");
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            response.addCookie(refreshTokenCookie);
            httpServletRequest.getCookies();
            System.out.println("đã vào dc response add cookie");
            List<String> roles = new ArrayList<>();
            for (Role role : user.getRoles()) {
                roles.add(role.getRole());
            }
            AuthenticationResponse data = AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .roles(roles)
                    .userId(user.getId())
                    .build();
            return ResponseEntity.ok(ApiResponse.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message(SuccessMessage.AUTHENTICATED)
                    .data(data)
                    .build());
        }catch (FirebaseAuthException e) {
            System.out.println(e.getAuthErrorCode());
            if (e.getAuthErrorCode().equals(AuthErrorCode.EXPIRED_ID_TOKEN)) {
                throw new AppException(ErrorCode.FIREBASE_KEY_EXPIRED);
            }
            throw new AppException(ErrorCode.FIREBASE_KEY_INVALID);
        }
    }
}
