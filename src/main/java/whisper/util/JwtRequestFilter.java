package whisper.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import whisper.entity.UserEntity;
import whisper.repository.UserRepository;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/auth/register", "/api/auth/login", // "/",
            "/login", "/home", "/join", "/joinProc",
            "/my-swagger-ui/**", "/my-api-docs/**", "/v3/api-docs",
            "/v3/api-docs.yaml", "/swagger-ui.html", "/swagger-resources/**",
            "/webjars/**", "/swagger-ui/**", "/error"
    );

    private static boolean isExcludedPath(String uri) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (uri.startsWith(excludedPath)) {
            	System.out.println(uri);
            	System.out.println(excludedPath);
            	
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (isExcludedPath(uri)) {
        	filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = extractJwtFromRequest(request);
        if (jwtToken != null) {
            try {
                String username = jwtUtils.extractUsername(jwtToken);
                UserEntity userEntity = userRepository.findByEmail(username);

                if (userEntity != null && jwtUtils.validateToken(jwtToken, userEntity)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userEntity, null, userEntity.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException | SignatureException | MalformedJwtException e) {
                log.warn("JWT validation failed: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

















