package com.indore.repository;

import com.indore.entity.Session;
import com.indore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session,Long> {
    List<Session> findByUserAndActiveTrue(User user);

    Optional<Session> findByToken(String token);
}
