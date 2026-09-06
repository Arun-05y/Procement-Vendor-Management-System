package com.procurea.procurementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 35, unique = true)
    private ERole name;

    public enum ERole {
        ROLE_ADMIN,
        ROLE_PROCUREMENT_MANAGER,
        ROLE_PROCUREMENT_EXECUTIVE,
        ROLE_FINANCE,
        ROLE_EMPLOYEE,
        ROLE_VENDOR
    }
}
