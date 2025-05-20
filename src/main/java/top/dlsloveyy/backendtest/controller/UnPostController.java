package top.dlsloveyy.backendtest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.entity.UnPost;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.UnPostRepository;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.service.AuditService;
import top.dlsloveyy.backendtest.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/post")
public class UnPostController {

    @Autowired
    private UnPostRepository unPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil; // ✅ 注入 JwtUtil Bean

    @Autowired
    private AuditService auditService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitPost(@RequestBody Map<String, String> payload,
                                        HttpServletRequest request) {
        String title = payload.get("title");
        String content = payload.get("content");

        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("code", 400, "message", "标题或内容不能为空"));
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "未登录或Token缺失"));
        }

        token = token.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "Token无效"));
        }

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "用户不存在"));
        }

        User user = optionalUser.get();

        UnPost post = new UnPost();
        post.setTitle(title);
        post.setContent(content);
        post.setStatus("待审核");
        post.setCreateTime(LocalDateTime.now());
        post.setAuthor(username);
        post.setAuthorId(user.getId());

        unPostRepository.save(post);

        // ✅ 调用自动审核逻辑
        auditService.autoAuditAll();

        return ResponseEntity.ok(Map.of("code", 200, "message", "发帖成功，正在审核"));
    }

}
