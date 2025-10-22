package com.mock_project_java_cevc_hung.hunglpmockjava.repository;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
}
