package com.useradmin.repository;

import com.useradmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

//    @Query("SELECT u FROM User u WHERE u.status = :status")
//    List<User> findByStatus(@Param("status") String status);
//
//    @Modifying
//    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :id")
//    void updateLastLogin(@Param("id") Long id, @Param("lastLogin") LocalDateTime lastLogin);

}
