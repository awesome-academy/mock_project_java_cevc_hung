package com.mock_project_java_cevc_hung.hunglpmockjava.repository;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long>, JpaSpecificationExecutor<ReviewEntity> {
}
