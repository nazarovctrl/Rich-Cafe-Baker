package com.example.repository;

import com.example.entity.MealEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MealsRepository extends CrudRepository<MealEntity,Integer> {
    List<MealEntity> findByMenu_Name(String name);

    Optional<MealEntity> findByName(String name);

    boolean existsByName(String name);
}
