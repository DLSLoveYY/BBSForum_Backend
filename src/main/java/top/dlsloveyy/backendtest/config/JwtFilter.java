package top.dlsloveyy.backendtest.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.util.JwtUtil;

import java.io.IOException;

@Component
public class JwtFilter implements Filter {

    @Autowired
    private UserRepository userRepository;

    public static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            if (username != null) {
                User user = userRepository.findByUsername(username);
                if (user != null) {
                    currentUser.set(user);
                }
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            currentUser.remove(); // 避免内存泄漏
        }
    }
}
