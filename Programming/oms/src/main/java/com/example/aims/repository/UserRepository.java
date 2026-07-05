package com.example.aims.repository;

import com.example.aims.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUserNameIgnoreCase(String userName);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByUserId(Integer userId);

    @Query(
            value = """
                    SELECT r.role_name
                    FROM users_roles ur
                    JOIN role r ON r.role_id = ur.roles_role_id
                    WHERE ur.users_user_id = :userId
                    """,
            nativeQuery = true)
    List<String> findRoleNamesByUserId(@Param("userId") Integer userId);
}
