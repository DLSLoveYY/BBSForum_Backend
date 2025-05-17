package top.dlsloveyy.backendtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.dlsloveyy.backendtest.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // ✅ 获取指定帖子下所有父子评论（用于嵌套）
    List<Comment> findByPostIdAndParentIdOrderByCreateTimeAsc(Long postId, Long parentId);

    // ✅ 新增：统计某个帖子下的评论数
    Long countAllByPostId(Long postId);

    Long countByPostId(Long postId);
}
