package whisper.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import whisper.dto.LoginRequest;
import whisper.dto.UserRequest;
import whisper.dto.UserResponse;
import whisper.service.UserService;
import whisper.util.JwtUtils;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/register")
    public String registerUser(@RequestBody UserRequest userRequest) {
        log.info("Registering new user with email: {}", userRequest.getEmail());
        userService.registerUser(userRequest);
        log.info("User registered successfully with email: {}", userRequest.getEmail());
        return "User registered successfully";
    }

    @Operation(summary = "로그인", description = "사용자 인증을 수행합니다.")
    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest loginRequest) {
        log.info("Authenticating user with email: {}", loginRequest.getEmail());
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);
        log.info("User authenticated successfully with email: {}", loginRequest.getEmail());

        return new UserResponse(jwt, userDetails.getUsername());
    }
}















