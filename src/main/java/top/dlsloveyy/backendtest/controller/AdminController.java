package top.dlsloveyy.backendtest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.entity.Post;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.PostRepository;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.util.JwtUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;
    /**
     * 最高管理员登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            String token = jwtUtil.generateToken(username);
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "登录成功",
                    "token", token
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                    "code", 401,
                    "message", "账号或密码错误"
            ));
        }
    }

    /**
     * 查看所有注册用户（仅管理员）
     */
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if (!adminUsername.equals(username)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限访问"));
        }

        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(Map.of("code", 200, "users", users));
    }

    /**
     * 设置某个用户为管理员（仅最高管理员）
     */
    @PostMapping("/setAdmin")
    public ResponseEntity<?> setAdmin(@RequestBody Map<String, String> payload,
                                      @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if (!adminUsername.equals(username)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限操作"));
        }

        String targetUsername = payload.get("username");
        Optional<User> optionalUser = userRepository.findByUsername(targetUsername);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "用户不存在"));
        }

        User user = optionalUser.get();
        user.setIsAdmin(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("code", 200, "message", "已将用户设为管理员"));
    }

    @PostMapping("/makeAdmin")
    public ResponseEntity<?> makeAdmin(@RequestBody Map<String, String> payload,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String operator = jwtUtil.getUsernameFromToken(token);
        if (!"dlsloveyy".equals(operator)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限"));
        }

        String username = payload.get("username");
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "用户不存在"));
        }

        User user = optionalUser.get();
        user.setIsAdmin(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("code", 200, "message", "已设为管理员"));
    }

    @PostMapping("/ban")
    public ResponseEntity<?> banUser(@RequestBody Map<String, String> payload,
                                     @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String operator = jwtUtil.getUsernameFromToken(token);
        if (!"dlsloveyy".equals(operator)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限"));
        }

        String username = payload.get("username");
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "用户不存在"));
        }

        User user = optionalUser.get();
        user.setEnabled(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("code", 200, "message", "已封禁该账号"));
    }

    @PostMapping("/unban")
    public ResponseEntity<?> unbanUser(@RequestBody Map<String, String> payload,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String operator = jwtUtil.getUsernameFromToken(token);
        if (!"dlsloveyy".equals(operator)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限"));
        }

        String username = payload.get("username");
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "用户不存在"));
        }

        User user = optionalUser.get();
        user.setEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("code", 200, "message", "已解除封禁"));
    }

    @PostMapping("/unsetAdmin")
    public ResponseEntity<?> unsetAdmin(@RequestBody Map<String, String> payload,
                                        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String operator = jwtUtil.getUsernameFromToken(token);
        if (!"dlsloveyy".equals(operator)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限"));
        }

        String username = payload.get("username");
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "用户不存在"));
        }

        User user = optionalUser.get();
        user.setIsAdmin(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("code", 200, "message", "已取消管理员权限"));
    }

    @GetMapping("/userPage")
    public Map<String, Object> getUserPage(@RequestParam int page,
                                           @RequestParam int size,
                                           @RequestHeader("Authorization") String authHeader) {
        String username = jwtUtil.getUsernameFromToken(authHeader.substring(7));
        if (!"dlsloveyy".equals(username)) {
            return Map.of("code", 403, "message", "无权限");
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> result = userRepository.findAll(pageable);

        return Map.of("code", 200, "data", Map.of(
                "records", result.getContent(),
                "total", result.getTotalElements()
        ));
    }
    /**
     * 发布公告（仅最高管理员）
     */
    @PostMapping("/announce")
    public ResponseEntity<?> publishAnnouncement(@RequestBody Map<String, String> payload,
                                                 @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if (!adminUsername.equals(username)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限"));
        }

        String title = payload.get("title");
        String content = payload.get("content");

        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "标题和内容不能为空"));
        }

        Post announcement = new Post();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setCreateTime(java.time.LocalDateTime.now());
        announcement.setViews(0);
        announcement.setLikes(0);
        announcement.setComments(0);
        announcement.setFeatured(false);
        announcement.setIsNotice(true);
        announcement.setAuthor("管理员"); // 你也可以设为 adminUsername

        // 不关联具体用户
        announcement.setUser(null);

        postRepository.save(announcement);
        return ResponseEntity.ok(Map.of("code", 200, "message", "公告发布成功"));
    }

}
