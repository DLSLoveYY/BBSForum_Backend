package top.dlsloveyy.backendtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.config.JwtFilter;
import top.dlsloveyy.backendtest.entity.Post;
import top.dlsloveyy.backendtest.entity.PostLike;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.PostLikeRepository;
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

    @Autowired
    private PostLikeRepository postLikeRepository;

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

    // ✅ 前端使用：分页帖子数据（featured 优先排序）
    @GetMapping("/page")
    public Map<String, Object> getPostPage(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "8") int size) {

        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(
                        Sort.Order.desc("isNotice"),   // 公告置顶
                        Sort.Order.desc("featured"),   // 精华次之
                        Sort.Order.desc("createTime")  // 然后按时间倒序
                ));

        Page<Post> postPage = postRepository.findAll(pageable);
        List<Post> posts = postPage.getContent();

        List<Map<String, Object>> postList = posts.stream().map(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("author", post.getAuthor());
            map.put("createTime", post.getCreateTime());
            map.put("views", post.getViews());
            map.put("likes", post.getLikes());
            map.put("featured", post.getFeatured());
            map.put("isNotice", post.getIsNotice());  // 确保这个字段返回
            Long count = commentRepository.countByPostId(post.getId());
            map.put("comments", count);
            return map;
        }).toList();

        return Map.of(
                "total", postPage.getTotalElements(),
                "posts", postList
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
    public ResponseEntity<?> getPostDetail(@RequestParam Long id,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
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

            result.put("author", authorMap);
        } else {
            result.put("author", Map.of("username", "未知", "avatar", ""));
        }

        // ✅ 添加点赞状态（当前用户是否点赞过该帖子）
        boolean liked = false;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                userRepository.findByUsername(username).ifPresent(user -> {
                    boolean hasLiked = postLikeRepository.existsByUserAndPost(user, post);
                    result.put("liked", hasLiked);  // ✅ 返回点赞状态
                });
            }
        } else {
            result.put("liked", false);  // 未登录默认未点赞
        }

        return ResponseEntity.ok(Map.of("code", 200, "data", result));
    }


    @PutMapping("/{id}/view")
    public ResponseEntity<?> increaseView(@PathVariable Long id) {
        Optional<Post> optional = postRepository.findById(id);
        if (optional.isPresent()) {
            Post post = optional.get();
            post.setViews(post.getViews() + 1);
            postRepository.save(post);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "帖子不存在"));
        }
    }
    @PutMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id,
                                      @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // 去掉 "Bearer "
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "未登录"));
        }

        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "帖子不存在"));
        }

        Post post = optionalPost.get();

        // 检查是否已点赞过
        if (postLikeRepository.existsByUserAndPost(user, post)) {
            return ResponseEntity.status(400).body(Map.of("code", 400, "message", "您已经点赞过该帖子"));
        }

        // 保存点赞记录
        PostLike like = new PostLike();
        like.setUser(user);
        like.setPost(post);
        like.setCreateTime(LocalDateTime.now());
        postLikeRepository.save(like);

        // 点赞数 +1
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);

        return ResponseEntity.ok(Map.of("code", 200, "message", "点赞成功", "likes", post.getLikes()));
    }

    @PutMapping("/{id}/unlike")
    public ResponseEntity<?> unlikePost(@PathVariable Long id,
                                        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "未登录"));
        }
        User user = optionalUser.get();

        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "帖子不存在"));
        }
        Post post = optionalPost.get();

        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);
        if (existingLike.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("code", 400, "message", "您尚未点赞该帖子"));
        }

        postLikeRepository.delete(existingLike.get());
        post.setLikes(Math.max(0, post.getLikes() - 1));  // 防止负数
        postRepository.save(post);

        return ResponseEntity.ok(Map.of("code", 200, "message", "已取消点赞", "likes", post.getLikes()));
    }
    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedPosts() {
        List<Post> featuredPosts = postRepository.findByFeaturedTrueOrderByCreateTimeDesc();

        // 可选：补充评论数量
        for (Post post : featuredPosts) {
            Long commentCount = commentRepository.countByPostId(post.getId());
            post.setComments(commentCount.intValue());
        }

        return ResponseEntity.ok(Map.of("code", 200, "posts", featuredPosts));
    }

    @GetMapping("/hot")
    public ResponseEntity<?> getHotPosts() {
        List<Post> allPosts = postRepository.findAll();

        List<Map<String, Object>> hotList = allPosts.stream().map(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("author", post.getAuthor());
            map.put("createTime", post.getCreateTime());
            map.put("views", post.getViews());
            map.put("likes", post.getLikes());
            map.put("featured", post.getFeatured());
            map.put("isNotice", post.getIsNotice()); // ✅ 补充公告标记

            Long commentCount = commentRepository.countByPostId(post.getId());
            map.put("comments", commentCount);

            // ✅ 热度评分计算（你可自行微调权重）
            double score = post.getViews() * 1.2 + commentCount * 3 + post.getLikes() * 4;
            map.put("score", score);

            return map;
        }).sorted((a, b) -> {
            // ✅ 排序规则：公告 > 精华 > 热度
            boolean aNotice = Boolean.TRUE.equals(a.get("isNotice"));
            boolean bNotice = Boolean.TRUE.equals(b.get("isNotice"));
            if (aNotice != bNotice) return Boolean.compare(bNotice, aNotice); // 公告先

            boolean aFeatured = Boolean.TRUE.equals(a.get("featured"));
            boolean bFeatured = Boolean.TRUE.equals(b.get("featured"));
            if (aFeatured != bFeatured) return Boolean.compare(bFeatured, aFeatured); // 精华次之

            return Double.compare((Double) b.get("score"), (Double) a.get("score")); // 再按热度
        }).toList();

        return ResponseEntity.ok(Map.of("code", 200, "posts", hotList));
    }

    @GetMapping("/notice")
    public ResponseEntity<?> getNoticePosts() {
        List<Post> notices = postRepository.findByIsNoticeTrueOrderByCreateTimeDesc();

        for (Post post : notices) {
            Long commentCount = commentRepository.countByPostId(post.getId());
            post.setComments(commentCount.intValue());
        }

        return ResponseEntity.ok(Map.of("code", 200, "posts", notices));
    }

}
