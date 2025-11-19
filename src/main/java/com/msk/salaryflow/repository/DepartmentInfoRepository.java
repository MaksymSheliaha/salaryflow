package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.DepartmentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DepartmentInfoRepository extends JpaRepository<DepartmentInfo, UUID> {
}
