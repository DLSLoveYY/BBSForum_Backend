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

        // ✅ 统计该 postId 的评论总数
        Long count = commentRepository.countByPostId(comment.getPostId());

        // ✅ 更新对应帖子中的 comments 字段
        Optional<Post> optionalPost = postRepository.findById(comment.getPostId());
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setComments(count.intValue());
            postRepository.save(post);
        }

        return "评论成功";
    }

    // ✅ 嵌套评论树结构查询（带作者名）
    @GetMapping("/tree")
    public List<Map<String, Object>> getNestedComments(@RequestParam Long postId) {
        return buildCommentTree(postId, 0L); // parentId = 0 表示一级评论
    }

    // ✅ 递归构建评论树结构（包含作者名）
    private List<Map<String, Object>> buildCommentTree(Long postId, Long parentId) {
        List<Comment> comments = commentRepository.findByPostIdAndParentIdOrderByCreateTimeAsc(postId, parentId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Comment comment : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("author", comment.getUser().getUsername()); // 从 user 实体中获取用户名
            map.put("createTime", comment.getCreateTime());
            map.put("parentId", comment.getParentId());
            map.put("replies", buildCommentTree(postId, comment.getId())); // 递归查找子评论

            result.add(map);
        }

        return result;
    }
}
