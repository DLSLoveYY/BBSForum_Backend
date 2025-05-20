package top.dlsloveyy.backendtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.dlsloveyy.backendtest.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ 修改：返回 Optional<User>
    Optional<User> findByUsername(String username);

    // 判断用户名是否已存在（用于注册校验）
    boolean existsByUsername(String username);
}
