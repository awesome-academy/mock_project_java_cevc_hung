package com.mock_project_java_cevc_hung.hunglpmockjava.repository;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ActivityLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, Long>, JpaSpecificationExecutor<ActivityLogEntity> {
}
