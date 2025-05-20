package top.dlsloveyy.backendtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.entity.Comment;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.CommentRepository;
import top.dlsloveyy.backendtest.repository.PostRepository;
import top.dlsloveyy.backendtest.repository.UserRepository;
import top.dlsloveyy.backendtest.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ 添加评论
    @PostMapping("/add")
    public ResponseEntity<?> addComment(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody Comment comment) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("未登录用户不能发表评论");
        }

        String username = jwtUtil.extractUsername(authHeader);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "用户不存在"));
        }

        User user = optionalUser.get();
        comment.setUser(user);
        comment.setCreateTime(LocalDateTime.now());
        commentRepository.save(comment);

        Long count = commentRepository.countByPostId(comment.getPostId());
        postRepository.findById(comment.getPostId()).ifPresent(post -> {
            post.setComments(count.intValue());
            postRepository.save(post);
        });

        return ResponseEntity.ok(Map.of("code", 200, "message", "评论成功"));
    }

    // ✅ 平铺式列表（不变）
    @GetMapping("/list")
    public Map<String, Object> getFlatCommentList(@RequestParam Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreateTimeAsc(postId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Comment c : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("content", c.getContent());
            map.put("createTime", c.getCreateTime());

            User user = c.getUser();
            if (user != null) {
                map.put("user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "avatar", user.getAvatar()
                ));
            } else {
                map.put("user", null);
            }

            result.add(map);
        }

        return Map.of("code", 200, "comments", result);
    }

    // ✅ 嵌套结构入口
    @GetMapping("/tree")
    public List<Map<String, Object>> getNestedComments(@RequestParam Long postId) {
        List<Comment> topLevel = commentRepository.findByPostIdAndParentIdIsNullOrderByCreateTimeAsc(postId);
        return buildCommentTree(postId, topLevel);
    }

    // ✅ 修复：构建树状结构（接收顶层列表）
    private List<Map<String, Object>> buildCommentTree(Long postId, List<Comment> topComments) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment comment : topComments) {
            result.add(convertCommentToMap(postId, comment));
        }
        return result;
    }

    // ✅ 原始递归构建（根据 parentId）
    private List<Map<String, Object>> buildCommentTree(Long postId, Long parentId) {
        List<Comment> children = commentRepository.findByPostIdAndParentIdOrderByCreateTimeAsc(postId, parentId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment child : children) {
            result.add(convertCommentToMap(postId, child));
        }
        return result;
    }

    // ✅ 核心转换逻辑：Comment → Map，包含 user、replyTo、replies
    private Map<String, Object> convertCommentToMap(Long postId, Comment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("content", comment.getContent());
        map.put("createTime", comment.getCreateTime());
        map.put("parentId", comment.getParentId());

        // user
        User user = comment.getUser();
        if (user != null) {
            map.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "avatar", user.getAvatar()
            ));
        } else {
            map.put("user", null);
        }

        // replyTo 信息
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentRepository.findById(comment.getParentId()).ifPresent(parent -> {
                Map<String, Object> replyTo = new HashMap<>();
                if (parent.getUser() != null) {
                    replyTo.put("username", parent.getUser().getUsername());
                }
                replyTo.put("content", simplifyQuotedContent(parent.getContent()));
                map.put("replyTo", replyTo);
            });
        }

        // 递归构建子评论
        List<Map<String, Object>> replies = buildCommentTree(postId, comment.getId());
        map.put("replies", replies != null ? replies : new ArrayList<>());

        return map;
    }

    /**
     * 简化引用内容：图片替换为【图片】，截断太长内容
     */
    private String simplifyQuotedContent(String raw) {
        if (raw == null) return "";

        // 替换 Markdown 图片语法为 【图片】
        String noImage = raw.replaceAll("!\\[[^\\]]*\\]\\([^\\)]*\\)", "【图片】");

        // 可选：限制长度，避免引用太长
        if (noImage.length() > 60) {
            noImage = noImage.substring(0, 60) + "...";
        }

        return noImage;
    }

    @GetMapping("/flat")
    public List<Map<String, Object>> getFlatComments(@RequestParam Long postId) {
        List<Comment> all = commentRepository.findByPostIdOrderByCreateTimeAsc(postId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Comment comment : all) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("createTime", comment.getCreateTime());
            map.put("parentId", comment.getParentId());

            // 用户信息
            User user = comment.getUser();
            if (user != null) {
                map.put("user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "avatar", user.getAvatar()
                ));
            }

            // replyTo 信息
            if (comment.getParentId() != null && comment.getParentId() > 0) {
                commentRepository.findById(comment.getParentId()).ifPresent(parent -> {
                    Map<String, Object> replyTo = new HashMap<>();
                    if (parent.getUser() != null) {
                        replyTo.put("username", parent.getUser().getUsername());
                    }
                    replyTo.put("content", simplifyQuotedContent(parent.getContent()));
                    map.put("replyTo", replyTo);
                });
            }

            result.add(map);
        }

        return result;
    }

}
