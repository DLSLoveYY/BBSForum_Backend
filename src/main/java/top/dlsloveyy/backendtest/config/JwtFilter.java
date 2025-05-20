package top.dlsloveyy.backendtest.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.util.JwtUtil;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtFilter implements Filter {

    private UserRepository userRepository;
    private JwtUtil jwtUtil;

    public static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    @Override
    public void init(FilterConfig filterConfig) {
        // 手动获取 Spring 容器中的 Bean
        ServletContext servletContext = filterConfig.getServletContext();
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

        this.userRepository = ctx.getBean(UserRepository.class);
        this.jwtUtil = ctx.getBean(JwtUtil.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                Optional<User> optionalUser = userRepository.findByUsername(username);
                optionalUser.ifPresent(currentUser::set); // ✅ 安全设置当前用户
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            currentUser.remove(); // 避免内存泄漏
        }
    }
}
