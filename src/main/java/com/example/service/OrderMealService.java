package com.example.service;

import com.example.entity.MealEntity;
import com.example.entity.OrderMealEntity;
import com.example.enums.OrdersStatus;
import com.example.repository.OrderMealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderMealService {

    @Autowired
    private OrderMealRepository repository;

    public void add(Integer orderId, MealEntity meal, int count) {
        OrderMealEntity entity = new OrderMealEntity();

        entity.setOrderId(orderId);
        entity.setMeal(meal);
        entity.setQuantity(count);

        repository.save(entity);

    }

    public List<OrderMealEntity> getNOTCONFIRMEDOderMealList(Long chatId) {

        return repository.getNotConfirmedListByUserId(chatId);
    }

    public List<OrderMealEntity> getListByOrderId(Integer id) {
        return repository.findByOrderId(id);
    }


}
