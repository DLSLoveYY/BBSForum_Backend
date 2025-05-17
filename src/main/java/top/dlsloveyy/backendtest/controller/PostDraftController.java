package top.dlsloveyy.backendtest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.entity.DraftPost;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.DraftPostRepository;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.util.JwtUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/draft")
public class PostDraftController {

    @Autowired
    private DraftPostRepository draftPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;  // ✅ 注入 JwtUtil Bean

    @PostMapping("/save")
    @Transactional
    public ResponseEntity<?> saveDraft(@RequestBody Map<String, String> payload,
                                       HttpServletRequest request) {
        String title = payload.get("title");
        String content = payload.get("content");

        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("code", 1, "message", "标题和内容不能为空"));
        }

        // 获取 token 并解析用户名
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("code", 2, "message", "未授权"));
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("code", 3, "message", "无效的 token"));
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("code", 4, "message", "用户不存在"));
        }

        // ✅ 删除该用户的旧草稿
        draftPostRepository.deleteAllByAuthorId(user.getId());

        // ✅ 保存新草稿
        DraftPost draft = new DraftPost();
        draft.setTitle(title);
        draft.setContent(content);
        draft.setAuthor(username);
        draft.setAuthorId(user.getId());

        draftPostRepository.save(draft);

        return ResponseEntity.ok(Map.of("code", 0, "message", "草稿保存成功"));
    }


    @GetMapping("/latest")
    public ResponseEntity<?> getLatestDraft(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("code", 1, "message", "未授权"));
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);  // ✅ 改为实例方法
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("code", 2, "message", "无效 token"));
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("code", 3, "message", "用户不存在"));
        }

        // 获取该用户的最近草稿（按时间倒序）
        List<DraftPost> drafts = draftPostRepository.findByAuthorIdOrderByCreateTimeDesc(user.getId());
        if (drafts.isEmpty()) {
            return ResponseEntity.ok(Map.of("code", 0, "message", "无草稿", "data", null));
        }

        DraftPost latest = drafts.get(0);
        return ResponseEntity.ok(Map.of(
                "code", 0,
                "message", "成功",
                "data", Map.of(
                        "title", latest.getTitle(),
                        "content", latest.getContent()
                )
        ));
    }
}
