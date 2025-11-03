package com.mock_project_java_cevc_hung.hunglpmockjava.repository;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<UserEntity> findTop5ByOrderByCreatedAtDesc();
}
