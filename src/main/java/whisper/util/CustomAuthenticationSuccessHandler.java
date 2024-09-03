package whisper.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import whisper.entity.UserEntity;
import whisper.repository.UserRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import io.jsonwebtoken.security.Keys; // (추가)

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Environment env;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername());

        Instant now = Instant.now();
        Long expirationTime = Long.parseLong(env.getProperty("jwt.expiration")); // (수정)

        String secret = env.getProperty("jwt.secret");
        Key hmacKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // (수정)

        String jwtToken = Jwts.builder()
                .claim("name", userEntity.getName())
                .claim("email", userEntity.getEmail())
                .setSubject(userEntity.getUsername())
                .setId(String.valueOf(userEntity.getId()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expirationTime, ChronoUnit.MILLIS)))
                .signWith(hmacKey) // (수정)
                .compact();
        log.debug("Generated JWT Token on Authentication Success: {}", jwtToken);

        request.getSession().setAttribute("user", userEntity);

        response.setHeader("token", jwtToken);
        // response.sendRedirect("/home"); // 로그인 성공 시 홈 페이지로 이동
    }
}





