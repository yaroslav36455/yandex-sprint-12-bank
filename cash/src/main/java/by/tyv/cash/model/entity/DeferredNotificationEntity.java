package by.tyv.cash.model.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Accessors(chain=true)
@Table("deferred_notification")
public class DeferredNotificationEntity {
    @Id
    private Long id;
    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("login")
    private String login;

    @Column("message")
    private String message;

    @Column("status")
    private String status;
}
