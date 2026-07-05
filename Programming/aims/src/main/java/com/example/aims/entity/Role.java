package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roleId")
    Integer roleId;

    @Column(name = "roleName", nullable = false, unique = true)
    String roleName;

    @Column(name = "description")
    String description;

    // Many Roles can be assigned to many Users
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    Set<User> users = new HashSet<>();
}
