package top.dlsloveyy.backendtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.config.JwtFilter;
import top.dlsloveyy.backendtest.entity.Comment;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.CommentRepository;
import top.dlsloveyy.backendtest.repository.PostRepository;
import top.dlsloveyy.backendtest.entity.Post;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    // ✅ 添加评论（绑定当前登录用户）
    @PostMapping("/add")
    public String addComment(@RequestBody Comment comment) {
        comment.setCreateTime(LocalDateTime.now());
        commentRepository.save(comment);

        // ✅ 更新帖子评论总数
        Long count = commentRepository.countByPostId(comment.getPostId());
        postRepository.findById(comment.getPostId()).ifPresent(post -> {
            post.setComments(count.intValue());
            postRepository.save(post);
        });

        return "评论成功";
    }

    // ✅ 扁平式楼层评论（用于贴吧式展示）
    @GetMapping("/list")
    public Map<String, Object> getFlatCommentList(@RequestParam Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreateTimeAsc(postId);

        // 封装结果带用户名 + 头像（安全判空）
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment c : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("content", c.getContent());
            map.put("createTime", c.getCreateTime());

            User user = c.getUser();
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("avatar", user.getAvatar());
                map.put("user", userMap);
            } else {
                map.put("user", null); // 防止空指针
            }

            result.add(map);
        }

        return Map.of("code", 200, "comments", result);
    }


    // ✅ 嵌套树结构（可用于“回复”模式）
    @GetMapping("/tree")
    public List<Map<String, Object>> getNestedComments(@RequestParam Long postId) {
        return buildCommentTree(postId, 0L); // parentId = 0 表示一级评论
    }

    private List<Map<String, Object>> buildCommentTree(Long postId, Long parentId) {
        List<Comment> comments = commentRepository.findByPostIdAndParentIdOrderByCreateTimeAsc(postId, parentId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Comment comment : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("author", comment.getUser().getUsername());
            map.put("createTime", comment.getCreateTime());
            map.put("parentId", comment.getParentId());
            map.put("replies", buildCommentTree(postId, comment.getId()));
            result.add(map);
        }

        return result;
    }
}
