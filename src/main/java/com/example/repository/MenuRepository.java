package com.example.repository;

import com.example.entity.MenuEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MenuRepository extends CrudRepository<MenuEntity,Integer> {
    List<MenuEntity> findAll();

    boolean existsByName(String name);
}
