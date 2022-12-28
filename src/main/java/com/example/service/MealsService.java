package com.example.service;

import com.example.entity.MealEntity;
import com.example.entity.OrderMealEntity;
import com.example.entity.OrdersEntity;
import com.example.repository.MealsRepository;
import com.example.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

@Service
public class MealsService {

    @Autowired
    private MealsRepository mealsRepository;

    @Autowired
    private OrdersRepository ordersRepository;



    public Optional<MealEntity> findName(String text) {

        return mealsRepository.findByName(text);


    }

    public List<MealEntity> findNameList(String text) {

        return mealsRepository.findByMenu_Name(text);

    }

    public boolean isExists(Message message) {
        return mealsRepository.existsByName(message.getText());
    }

    public void createOrder(OrdersEntity ordersEntity) {
        ordersRepository.save(ordersEntity);
    }


}
