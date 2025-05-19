package top.dlsloveyy.backendtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.dlsloveyy.backendtest.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // ✅ 获取所有评论（用于平铺楼层展示）
    List<Comment> findByPostIdOrderByCreateTimeAsc(Long postId);

    // ✅ 获取一级评论（parentId 为 null）
    List<Comment> findByPostIdAndParentIdIsNullOrderByCreateTimeAsc(Long postId);

    // ✅ 获取子评论（parentId 非 null）
    List<Comment> findByPostIdAndParentIdOrderByCreateTimeAsc(Long postId, Long parentId);

    // ✅ 统计评论数（两种写法）
    Long countByPostId(Long postId);
    Long countAllByPostId(Long postId);
}
