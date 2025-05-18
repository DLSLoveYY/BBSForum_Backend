package top.dlsloveyy.backendtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.dlsloveyy.backendtest.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查找用户（用于登录）
    User findByUsername(String username);

    // 判断用户名是否已存在（用于注册校验）
    boolean existsByUsername(String username);
}
