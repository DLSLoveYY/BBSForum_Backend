package top.dlsloveyy.backendtest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.dlsloveyy.backendtest.entity.*;
import top.dlsloveyy.backendtest.repository.*;
import top.dlsloveyy.backendtest.util.SensitiveWordFilter;

import java.util.List;

@Service
public class AuditService {

    @Autowired
    private UnPostRepository unPostRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CheckPostRepository checkPostRepository;

    @Autowired
    private UserRepository userRepository;

    private static final List<String> SENSITIVE_WORDS = List.of("暴力", "枪支", "毒品", "爆炸", "政治", "辱骂",
            "恐怖", "反动", "攻击", "诈骗", "黑市", "走私");

    /**
     * 执行自动审核，将帖子分流至 post 表或 check_post 表
     */
    @Transactional
    public void autoAuditAll() {
        List<UnPost> pendingPosts = unPostRepository.findAll();

        for (UnPost post : pendingPosts) {
            String risk = assessRisk(post.getTitle(), post.getContent());

            if ("low".equals(risk)) {
                // 插入 post 表（正式发布）
                Post approved = new Post();
                approved.setTitle(post.getTitle());
                approved.setContent(post.getContent());
                approved.setAuthor(post.getAuthor());
                approved.setUser(userRepository.findById(post.getAuthorId()).orElse(null));
                approved.setCreateTime(post.getCreateTime());
                postRepository.save(approved);

            } else {
                // 插入 check_post 表（待人工审核）
                CheckPost checkPost = new CheckPost();
                checkPost.setTitle(post.getTitle());
                checkPost.setContent(post.getContent());
                checkPost.setAuthor(post.getAuthor());
                checkPost.setAuthorId(post.getAuthorId());
                checkPost.setCreateTime(post.getCreateTime());
                checkPost.setRiskLevel("high");
                checkPost.setStatus("pending");
                checkPostRepository.save(checkPost);
            }

            // 删除原 un_post 记录
            unPostRepository.delete(post);
        }
    }

    /**
     * 简单关键词自动审核：返回 low / high
     */
    private String assessRisk(String title, String content) {
        if (SensitiveWordFilter.contains(title) || SensitiveWordFilter.contains(content)) {
            return "high";
        }
        return "low";
    }

}
