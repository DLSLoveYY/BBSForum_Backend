package top.dlsloveyy.backendtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ✅ 注册接口（统一返回结构）
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.ok(Map.of("code", 400, "message", "用户名和密码不能为空"));
        }

        if (userRepository.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.ok(Map.of("code", 409, "message", "用户名已存在"));
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("code", 200, "message", "注册成功"));
    }

    // ✅ 登录接口（统一返回结构）
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        String username = loginUser.getUsername();
        String password = loginUser.getPassword();

        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.ok(Map.of("code", 401, "message", "用户名或密码错误"));
        }

        String token = JwtUtil.generateToken(username);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "登录成功",
                "token", token
        ));
    }
}
