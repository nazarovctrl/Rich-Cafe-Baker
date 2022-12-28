package com.example.admin.repository;

import com.example.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminMenuRepository extends JpaRepository<MenuEntity, Long> {
    Optional<MenuEntity> findAllByName(String name);
}
