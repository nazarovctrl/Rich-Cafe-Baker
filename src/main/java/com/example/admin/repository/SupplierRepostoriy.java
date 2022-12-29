package com.example.admin.repository;
import com.example.entity.AdminEntity;
import com.example.enums.UserStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepostoriy extends CrudRepository<AdminEntity,Integer> {


    List<AdminEntity> findByStatus(UserStatus admin);

    Optional<AdminEntity> findByPassword(String text);

    Optional<AdminEntity> findByPhone(String text);

}
