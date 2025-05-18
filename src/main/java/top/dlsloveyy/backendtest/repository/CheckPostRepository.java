package top.dlsloveyy.backendtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.dlsloveyy.backendtest.entity.CheckPost;

import java.util.List;

public interface CheckPostRepository extends JpaRepository<CheckPost, Long> {

    // 查询某位作者的所有审核中帖子
    List<CheckPost> findByAuthorId(Long authorId);

    // 可选：查询所有 pending 状态的帖子
    List<CheckPost> findByStatus(String status);
}
