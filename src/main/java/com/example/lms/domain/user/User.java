package com.example.lms.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * users 테이블 매핑 엔티티.
 * JPA는 검증용으로만 사용하고 CRUD는 JdbcTemplate로 처리한다.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String role;

    protected User() {}

    public User(Long id, String email, String name, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }
}
