package top.dlsloveyy.backendtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.entity.CheckPost;
import top.dlsloveyy.backendtest.entity.Post;
import top.dlsloveyy.backendtest.repository.CheckPostRepository;
import top.dlsloveyy.backendtest.repository.PostRepository;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.util.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkPost")
public class CheckPostController {

    @Autowired
    private CheckPostRepository checkPostRepository;

    @Autowired
    private PostRepository postRepository;   // ✅ 新增

    @Autowired
    private UserRepository userRepository;   // ✅ 新增

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    public ResponseEntity<?> getAllCheckPosts(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if (!"dlsloveyy".equals(username)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限访问"));
        }

        List<CheckPost> list = checkPostRepository.findAll();
        return ResponseEntity.ok(Map.of("code", 200, "data", list));
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approvePost(@RequestBody Map<String, Long> payload,
                                         @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if (!"dlsloveyy".equals(username)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限操作"));
        }

        Long id = payload.get("id");
        CheckPost cp = checkPostRepository.findById(id).orElse(null);
        if (cp == null) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "帖子不存在"));
        }

        Post post = new Post();
        post.setTitle(cp.getTitle());
        post.setContent(cp.getContent());
        post.setAuthor(cp.getAuthor());
        post.setUser(userRepository.findById(cp.getAuthorId()).orElse(null));
        post.setCreateTime(cp.getCreateTime());

        // ✅ 设置默认字段（视图统计/点赞等）
        post.setViews(0);
        post.setComments(0);
        post.setLikes(0);
        post.setFeatured(false);

        postRepository.save(post);
        checkPostRepository.delete(cp);

        return ResponseEntity.ok(Map.of("code", 200, "message", "已通过审核"));
    }


    @PostMapping("/delete")
    public ResponseEntity<?> deletePost(@RequestBody Map<String, Long> payload,
                                        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if (!"dlsloveyy".equals(username)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限操作"));
        }

        Long id = payload.get("id");
        if (!checkPostRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "帖子不存在"));
        }

        checkPostRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("code", 200, "message", "已删除帖子"));
    }
    @GetMapping("/page")
    public Map<String, Object> getCheckPostPage(@RequestParam int page,
                                                @RequestParam int size,
                                                @RequestHeader("Authorization") String authHeader) {
        String username = jwtUtil.getUsernameFromToken(authHeader.substring(7));
        if (!"dlsloveyy".equals(username)) {
            return Map.of("code", 403, "message", "无权限");
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<CheckPost> result = checkPostRepository.findAll(pageable);

        return Map.of("code", 200, "data", Map.of(
                "records", result.getContent(),
                "total", result.getTotalElements()
        ));
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getCheckPostDetail(@RequestParam Long id) {
        Optional<CheckPost> optional = checkPostRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "审核帖子不存在"));
        }
        return ResponseEntity.ok(Map.of("code", 200, "data", optional.get()));
    }

}
