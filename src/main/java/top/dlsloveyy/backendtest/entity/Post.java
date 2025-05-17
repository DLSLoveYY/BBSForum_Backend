package top.dlsloveyy.backendtest.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createTime;

    @ManyToOne
    @JoinColumn(name = "user_id")  // 外键列名
    private User user;  // 关联的作者对象
    private String author;

    @Column(nullable = false)
    private Integer views = 0;

    @Column(nullable = false)
    private Integer comments = 0;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(nullable = false)
    private Boolean featured = false;
}
