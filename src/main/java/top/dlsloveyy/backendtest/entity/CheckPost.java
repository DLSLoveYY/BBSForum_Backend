package top.dlsloveyy.backendtest.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "check_post")
public class CheckPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private String author;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    // 可选字段：风险等级
    @Column(nullable = false)
    private String riskLevel = "high"; // 默认就是 high

    // 可选字段：审核状态（如果你计划支持“退回”或“通过”）
    @Column(nullable = false)
    private String status = "pending"; // 或 reviewed/approved/rejected
}
