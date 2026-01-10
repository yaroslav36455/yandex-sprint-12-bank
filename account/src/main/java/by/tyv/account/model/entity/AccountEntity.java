package by.tyv.account.model.entity;

import by.tyv.account.enums.CurrencyCode;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain=true)
@Table("account")
public class AccountEntity {
    @Id
    private Long id;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("user_id")
    private Long userId;

    private BigDecimal balance;
    private CurrencyCode currency;
}
