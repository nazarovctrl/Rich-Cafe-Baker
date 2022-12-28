package com.example.service;

import com.example.entity.OrdersEntity;
import com.example.entity.ProfileEntity;
import com.example.enums.MethodType;
import com.example.enums.OrdersStatus;
import com.example.enums.Payment;
import com.example.repository.MenuRepository;
import com.example.repository.OrdersRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

@Service
public class OrdersService {

    private final OrdersRepository ordersRepository;

    private final MenuRepository menuRepository;

    private final AuthService authService;

    public OrdersService(OrdersRepository ordersRepository, MenuRepository menuRepository, AuthService authService) {
        this.ordersRepository = ordersRepository;
        this.menuRepository = menuRepository;
        this.authService = authService;
    }

    public List<OrdersEntity> handle(Message message) {
        List<OrdersEntity> ordersList = ordersRepository.findByProfile_Id(message.getChatId());

        if (ordersList != null) {
            return ordersList;
        }

        return null;
    }

    public void deleteByUserId(Long userId) {
        ordersRepository.deleteByProfile_Id(userId);
    }

    public boolean isExists(Message message) {
        return menuRepository.existsByName(message.getText());
    }


    public OrdersEntity add(OrdersEntity entity) {
        Optional<OrdersEntity> optional = ordersRepository.findByProfileAndStatusAndVisibleTrue(entity.getProfile(), OrdersStatus.NOT_CONFIRMED);
        if (optional.isPresent()) {
            return optional.get();
        }

        ordersRepository.save(entity);
        return entity;
    }

    public void changePayment(Long userId, Payment cash) {
        ProfileEntity profile = authService.findByUserId(userId);
        ordersRepository.changePayment(profile.getId(),cash);
    }

    public void changeMethodType(Long userId, MethodType methodType) {
        ProfileEntity profile = authService.findByUserId(userId);
        ordersRepository.changeMethodType(profile.getId(),methodType);
    }
}
