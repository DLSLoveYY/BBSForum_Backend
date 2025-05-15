package top.dlsloveyy.backendtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import top.dlsloveyy.backendtest.config.JwtFilter;
import top.dlsloveyy.backendtest.entity.Post;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @PostMapping("/add")
    public Map<String, Object> addPost(@RequestBody Post post) {
        User currentUser = JwtFilter.currentUser.get();
        if (currentUser == null) {
            return Map.of("code", 401, "message", "未登录，禁止发帖");
        }

        post.setCreateTime(LocalDateTime.now());
        post.setUser(currentUser);
        postRepository.save(post);

        return Map.of("code", 200, "message", "发帖成功");
    }


    // ✅ 查询所有帖子列表（不分页）
    @GetMapping("/list")
    public List<Post> getPostList() {
        return postRepository.findAll(Sort.by("createTime").descending());
    }

    // ✅ 分页查询帖子列表
    @GetMapping("/page")
    public Map<String, Object> getPostPage(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<Post> postPage = postRepository.findAll(pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("total", postPage.getTotalElements());
        result.put("posts", postPage.getContent());
        return result;
    }
}
