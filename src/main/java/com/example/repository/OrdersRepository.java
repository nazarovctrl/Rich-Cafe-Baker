package com.example.repository;

import com.example.entity.OrdersEntity;
import com.example.entity.ProfileEntity;
import com.example.enums.MethodType;
import com.example.enums.OrdersStatus;
import com.example.enums.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<OrdersEntity, Integer> {
    List<OrdersEntity> findAll();

    List<OrdersEntity> findByProfile_Id(Long userId);

    @Transactional
    void deleteByProfile_Id(Long userId);


    Optional<OrdersEntity> findByProfileAndStatusAndVisibleTrue(ProfileEntity profile, OrdersStatus status);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update OrdersEntity o  set o.payment=?2 where o.profile.id=?1 and o.visible=true and o.status='NOT_CONFIRMED' ")
    void changePayment(Integer id,Payment cash);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update OrdersEntity o  set o.methodType=?2 where o.profile.id=?1 and o.visible=true and o.status='NOT_CONFIRMED' ")
    void changeMethodType(Integer id, MethodType methodType);
}
