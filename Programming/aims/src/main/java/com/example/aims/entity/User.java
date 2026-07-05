package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    Integer userId;

    @Column(name = "userName", nullable = false)
    String userName;

    @Column(name = "email", nullable = false)
    String email;

    @Column(name = "hashedPassword", nullable = false)
    String hashedPassword;

    @Column(name = "avatarUrl")
    String avatarUrl;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    UserStatus status;

    // Self-referencing foreign key
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "createdBy", referencedColumnName = "userId")
    User createdByUser;

    // One User can create many Users
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "createdByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<User> createdUsers = new HashSet<>();

    // Many Users can have many Roles
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    Set<Role> roles = new HashSet<>();

    // One User can create many History records
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "createdByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<History> histories = new HashSet<>();

    // One User can have many Refunds
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<Refund> refunds = new HashSet<>();
}
