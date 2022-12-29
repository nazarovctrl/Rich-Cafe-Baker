package com.example.repository;

import com.example.entity.OrderMealEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderMealRepository extends CrudRepository<OrderMealEntity, Integer> {

    @Query("from OrderMealEntity as o " +
            " where o.order.profile.userId=?1 and o.order.status='NOT_CONFIRMED' " +
            " and o.order.visible=true and o.visible=true ")
    List<OrderMealEntity> getNotConfirmedListByUserId(Long profileId);


    List<OrderMealEntity> findByOrderId(Integer orderId);
}
