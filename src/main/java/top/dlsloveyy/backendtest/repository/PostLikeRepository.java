package top.dlsloveyy.backendtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.dlsloveyy.backendtest.entity.PostLike;
import top.dlsloveyy.backendtest.entity.User;
import top.dlsloveyy.backendtest.entity.Post;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<PostLike> findByUserAndPost(User user, Post post);
}
