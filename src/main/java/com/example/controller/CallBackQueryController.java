package com.example.controller;

import com.example.admin.service.DeliveryService;
import com.example.service.OrdersService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Controller
public class CallBackQueryController {

    private final OrdersService ordersService;


    private final DeliveryService deliveryService;

    @Lazy
    public CallBackQueryController(OrdersService ordersService, DeliveryService deliveryService) {
        this.ordersService = ordersService;
        this.deliveryService = deliveryService;
    }

    public void handleCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();


        String[] split = data.split("/");
        if (split[0].equals("cancelS")) {
            deliveryService.cancelDeliveryRequest(update);
            return;
        }

        if (split[0].equals("delivery")) {
            deliveryService.confirmDelivery(update);
            return;
        }

        if (split[0].equals("locA")) {
            deliveryService.getLocationForAdmin(update);
             return;
        }

        if (split[0].equals("locS")) {
            deliveryService.getLocationForSupplier(update);
            return;
        }


        if (split[0].equals("save")){
            ordersService.save(update);
            return;
        }

        if (split[0].equals("cancel")){
            ordersService.cancel(update);
            return;
        }

        if (split[0].equals("finish")){
            ordersService.finish(update);
        }

        if (split[0].equals("location")){
            deliveryService.getLocation(update);
        }

    }


}
