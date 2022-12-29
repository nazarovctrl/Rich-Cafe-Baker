package com.example.admin.repository;
import com.example.entity.MealEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AdminMealsRepository extends CrudRepository<MealEntity,Integer> {

    List<MealEntity> findAll();
    List<MealEntity> findByMenu_Id(Integer id);


    Optional<MealEntity> findByName(String name);
}
