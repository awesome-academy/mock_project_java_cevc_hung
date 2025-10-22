package com.mock_project_java_cevc_hung.hunglpmockjava.entity;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bank_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankAccountEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String bankName;
    private String accountNo;
    private String accountName;
    private String branch;
    @Builder.Default
    private Boolean isDefault = false;
}

