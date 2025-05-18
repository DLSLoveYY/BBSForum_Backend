package top.dlsloveyy.backendtest.entity;

import jakarta.persistence.*; // 如果不支持可改为 javax.persistence.*
        import lombok.Data;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean isAdmin = false;

    @Column(nullable = false)
    private Boolean enabled = true; // 表示账号是否启用

    @Column(name = "avatar")
    private String avatar; // 头像图片URL

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
