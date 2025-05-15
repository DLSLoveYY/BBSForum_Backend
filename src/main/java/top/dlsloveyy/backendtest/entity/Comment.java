package top.dlsloveyy.backendtest.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createTime;

    private Long postId;     // 所属帖子 ID

    private Long parentId;   // 父评论 ID，为 null 表示是一级评论

    @ManyToOne
    @JoinColumn(name = "user_id")  // 外键字段，指向 User 表的主键
    private User user;
}
