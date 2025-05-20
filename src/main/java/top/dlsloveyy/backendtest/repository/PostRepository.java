package top.dlsloveyy.backendtest.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import top.dlsloveyy.backendtest.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);
    List<Post> findByFeaturedTrueOrderByCreateTimeDesc();
    List<Post> findAllByOrderByFeaturedDescCreateTimeDesc();
    List<Post> findByIsNoticeTrueOrderByCreateTimeDesc();

}
