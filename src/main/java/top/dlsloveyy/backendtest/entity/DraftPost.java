package top.dlsloveyy.backendtest.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "draft_post")
public class DraftPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 作者ID，关联 User 表主键
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    // 作者用户名
    @Column(nullable = false)
    private String author;

    // 帖子标题
    @Column(nullable = false)
    private String title;

    // 帖子内容（Markdown 格式）
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 创建时间（自动填充）
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
