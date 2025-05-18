package top.dlsloveyy.backendtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.config.JwtFilter;
import top.dlsloveyy.backendtest.entity.Post;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.PostRepository;
import top.dlsloveyy.backendtest.repository.CommentRepository;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    // ✅ 发帖接口
    @PostMapping("/add")
    public Map<String, Object> addPost(@RequestBody Post post) {
        User currentUser = JwtFilter.currentUser.get();
        if (currentUser == null) {
            return Map.of("code", 401, "message", "未登录，禁止发帖");
        }

        post.setCreateTime(LocalDateTime.now());
        post.setUser(currentUser);
        post.setAuthor(currentUser.getUsername());
        post.setViews(0);
        post.setLikes(0);
        post.setComments(0);
        post.setFeatured(false);

        postRepository.save(post);
        return Map.of("code", 200, "message", "发帖成功");
    }

    // ✅ 前端使用：获取全部帖子（不分页）
    @GetMapping("/list")
    public List<Post> getPostList() {
        return postRepository.findAll(Sort.by("createTime").descending());
    }

    // ✅ 前端使用：分页帖子数据（旧结构）
    @GetMapping("/page")
    public Map<String, Object> getPostPage(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<Post> postPage = postRepository.findAll(pageable);
        List<Post> posts = postPage.getContent();
        for (Post post : posts) {
            Long count = commentRepository.countByPostId(post.getId());
            post.setComments(count.intValue());
        }

        return Map.of(
                "total", postPage.getTotalElements(),
                "posts", posts
        );
    }

    // ✅ 后台管理：删除帖子
    @PostMapping("/delete")
    public ResponseEntity<?> deletePost(@RequestBody Map<String, Long> payload,
                                        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if (!"dlsloveyy".equals(username)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限"));
        }

        Long id = payload.get("id");
        if (!postRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "帖子不存在"));
        }

        postRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("code", 200, "message", "帖子已删除"));
    }

    // ✅ 后台管理：精华切换
    @PostMapping("/toggleFeatured")
    public ResponseEntity<?> toggleFeatured(@RequestBody Map<String, Long> payload,
                                            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if (!"dlsloveyy".equals(username)) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限"));
        }

        Long id = payload.get("id");
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "帖子不存在"));
        }

        boolean current = Boolean.TRUE.equals(post.getFeatured());
        post.setFeatured(!current);
        postRepository.save(post);

        return ResponseEntity.ok(Map.of("code", 200, "message", current ? "已取消精华" : "已设为精华"));
    }

    // ✅ 后台分页接口（新版结构）
    @GetMapping("/adminPage")
    public Map<String, Object> getPostAdminPage(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestHeader("Authorization") String authHeader) {
        String username = jwtUtil.getUsernameFromToken(authHeader.substring(7));
        if (!"dlsloveyy".equals(username)) {
            return Map.of("code", 403, "message", "无权限");
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<Post> postPage = postRepository.findAll(pageable);

        List<Post> posts = postPage.getContent();
        for (Post post : posts) {
            Long commentCount = commentRepository.countByPostId(post.getId());
            post.setComments(commentCount.intValue());
        }

        return Map.of(
                "code", 200,
                "data", Map.of("records", posts, "total", postPage.getTotalElements())
        );
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getPostDetail(@RequestParam Long id) {
        Optional<Post> optional = postRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "帖子不存在"));
        }

        Post post = optional.get();

        Map<String, Object> result = new HashMap<>();
        result.put("id", post.getId());
        result.put("title", post.getTitle());
        result.put("content", post.getContent());
        result.put("createTime", post.getCreateTime());
        result.put("views", post.getViews());
        result.put("likes", post.getLikes());
        result.put("comments", post.getComments());
        result.put("featured", post.getFeatured());

        // ✅ 添加作者信息
        User author = post.getUser();
        if (author != null) {
            Map<String, Object> authorMap = new HashMap<>();
            authorMap.put("username", author.getUsername());

            String avatar = author.getAvatar();
            if (avatar != null && !avatar.startsWith("http")) {
                avatar = "http://localhost:8080" + avatar;
            }
            authorMap.put("avatar", avatar);

            result.put("author", authorMap); // ✅ 现在是对象
        } else {
            result.put("author", Map.of("username", "未知", "avatar", ""));
        }

        return ResponseEntity.ok(Map.of("code", 200, "data", result));
    }


}
