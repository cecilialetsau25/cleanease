package com.webapp.cleanease_laundry_system.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    User findByEmail(String email);

    @Transactional
    void deleteByEmail(String email);
}

