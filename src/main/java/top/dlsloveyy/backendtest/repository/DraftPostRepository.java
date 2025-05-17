package top.dlsloveyy.backendtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.dlsloveyy.backendtest.entity.DraftPost;

import java.util.List;

public interface DraftPostRepository extends JpaRepository<DraftPost, Long> {

    // 可选：根据作者 ID 查询所有草稿
    List<DraftPost> findByAuthorId(Long authorId);

    // 可选：根据作者用户名查询草稿
    List<DraftPost> findByAuthor(String author);

    List<DraftPost> findByAuthorIdOrderByCreateTimeDesc(Long authorId);

    void deleteAllByAuthorId(Long authorId);
}
