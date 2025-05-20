package top.dlsloveyy.backendtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.util.JwtUtil;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ 登录接口
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        String username = loginUser.getUsername();
        String password = loginUser.getPassword();

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty() || !optionalUser.get().getPassword().equals(password)) {
            return ResponseEntity.ok(Map.of("code", 401, "message", "用户名或密码错误"));
        }

        String token = jwtUtil.generateToken(username);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "登录成功",
                "token", token
        ));
    }

    // ✅ 注册接口
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }

        userRepository.save(user);
        return ResponseEntity.ok("注册成功");
    }

    // ✅ 获取用户信息
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("用户不存在");
        }

        User user = optionalUser.get();

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());
        result.put("signature", user.getSignature());
        result.put("gender", user.getGender());
        result.put("age", user.getAge());

        return ResponseEntity.ok(result);
    }

    // ✅ 更新用户信息
    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(@RequestHeader("Authorization") String token,
                                            @RequestBody Map<String, Object> updateData) {
        String username = jwtUtil.extractUsername(token);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("用户不存在");
        }

        User user = optionalUser.get();

        String newUsername = (String) updateData.get("username");
        String email = (String) updateData.get("email");
        String password = (String) updateData.get("password");
        String avatar = (String) updateData.get("avatar");
        String signature = (String) updateData.get("signature");
        String gender = (String) updateData.get("gender");
        Integer age = updateData.get("age") != null ? (Integer) updateData.get("age") : null;

        if (email != null) user.setEmail(email);
        if (password != null && !password.isEmpty()) user.setPassword(password);
        if (avatar != null) user.setAvatar(avatar);
        if (signature != null) user.setSignature(signature);
        if (gender != null) user.setGender(gender);
        if (age != null) user.setAge(age);

        // ✅ 检查用户名是否冲突
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
