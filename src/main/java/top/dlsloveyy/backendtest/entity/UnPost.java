package top.dlsloveyy.backendtest.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "un_post")
public class UnPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 作者用户名
    @Column(nullable = false)
    private String author;

    // 作者用户ID
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    // 帖子标题
    @Column(nullable = false)
    private String title;

    // Markdown内容
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 创建时间
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    // 状态（如 pending / approved / rejected）
    @Column(nullable = false)
    private String status;
}
