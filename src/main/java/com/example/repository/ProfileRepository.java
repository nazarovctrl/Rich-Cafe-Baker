package com.example.repository;

import com.example.entity.ProfileEntity;
import com.example.enums.UserStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends CrudRepository<ProfileEntity, Integer> {


    List<ProfileEntity> findAll();

    Optional<ProfileEntity> findByPhone(String phone);

    Optional<ProfileEntity> getByUserId(Long id);

    boolean existsByUserId(Long userId);


    boolean existsByPhone(String phone);

    Optional<ProfileEntity> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("update ProfileEntity p set p.status=?2 where p.userId=?1")
    void  changeVisibleByUserid(Long userId, UserStatus status);

}
