package top.dlsloveyy.backendtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.dlsloveyy.backendtest.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // ✅ 嵌套结构用（根据父评论 ID）
    List<Comment> findByPostIdAndParentIdOrderByCreateTimeAsc(Long postId, Long parentId);

    // ✅ 统计评论数量
    Long countAllByPostId(Long postId);
    Long countByPostId(Long postId);

    // ✅ 用于平铺楼层评论展示（PostDetail.vue 中）
    List<Comment> findByPostIdOrderByCreateTimeAsc(Long postId);
}
