package com.msk.salaryflow.repository;

import com.msk.salaryflow.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    @Query("SELECT e.id FROM Employee e WHERE " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<UUID> findIdsBySearchTerm(@Param("term") String term);

    // поддержка получения страницы по списку id (для корректной сортировки/пейджинга)
    Page<Employee> findAllByIdIn(List<UUID> ids, Pageable pageable);

    // Добавлено: получить список сущностей по списку id (используется для сохранения порядка, как в Department)
    List<Employee> findAllByIdIn(List<UUID> ids);
}