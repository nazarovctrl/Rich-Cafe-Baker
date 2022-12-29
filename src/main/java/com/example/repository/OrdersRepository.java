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


    @Override
    Optional<OrdersEntity> findById(Integer integer);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update OrdersEntity o  set o.visible=false where o.profile.id=?1 and o.visible=true and o.status='NOT_CONFIRMED' ")
    void deleteByProfile_Id(Integer profileId);


    Optional<OrdersEntity> findByProfileAndStatusAndVisibleTrue(ProfileEntity profile, OrdersStatus status);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update OrdersEntity o  set o.payment=?2 where o.profile.id=?1 and o.visible=true and o.status='NOT_CONFIRMED' ")
    void changePayment(Integer id, Payment cash);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update OrdersEntity o  set o.methodType=?2 where o.profile.id=?1 and o.visible=true and o.status='NOT_CONFIRMED' ")
    void changeMethodType(Integer id, MethodType methodType);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update OrdersEntity o  set o.latitude=?2 , o.longitude=?3 where o.profile.id=?1 and o.visible=true and o.status='NOT_CONFIRMED' ")
    void setLocation(Integer id, Double latitude, Double longitude);


    OrdersEntity findByProfile_UserIdAndVisibleAndStatus(Long userId, boolean visible, OrdersStatus status);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update OrdersEntity o  set o.status=?2 where o.profile.id=?1 and o.visible=true and o.status='NOT_CONFIRMED' ")
    void changeStatus(Integer id, OrdersStatus status);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update OrdersEntity o  set o.status=?2 where o.id=?1 ")
    void changeStatusById(Integer orderId, OrdersStatus status);


    @Query("from OrdersEntity o where o.profile.userId=?1 and o.status=?2 and o.visible=true ")
    List<OrdersEntity> getOrdersHistoryListByUserId(Long userId, OrdersStatus status);
}
