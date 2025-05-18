package top.dlsloveyy.backendtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.util.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ 登录接口（保持不变）
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        String username = loginUser.getUsername();
        String password = loginUser.getPassword();

        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.ok(Map.of("code", 401, "message", "用户名或密码错误"));
        }

        String token = jwtUtil.generateToken(username);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "登录成功",
                "token", token
        ));
    }

    // ✅ 注册接口（改为 @RequestBody）
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }

        userRepository.save(user);
        return ResponseEntity.ok("注册成功");
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }

        // 使用 put 构建，允许值为 null
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());

        return ResponseEntity.ok(result);
    }


    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(@RequestHeader("Authorization") String token,
                                            @RequestBody Map<String, String> updateData) {
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }
        String newUsername = updateData.get("username");
        String email = updateData.get("email");
        String password = updateData.get("password");
        String avatar = updateData.get("avatar");

        if (email != null) user.setEmail(email);
        if (password != null && !password.isEmpty()) user.setPassword(password);
        if (avatar != null) user.setAvatar(avatar);
        // ✅ 如果用户名有修改，则检查是否已存在（排除自己）
        if (newUsername != null && !newUsername.equals(user.getUsername())) {
            if (userRepository.existsByUsername(newUsername)) {
                return ResponseEntity.badRequest().body("该用户名已被占用");
            }
            user.setUsername(newUsername);
        }
        userRepository.save(user);
        return ResponseEntity.ok("更新成功");
    }


}
