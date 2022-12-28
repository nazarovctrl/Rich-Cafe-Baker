package com.example.service;

import com.example.myTelegramBot.MyTelegramBot;
import com.example.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddMenuService {

    @Autowired
    private MenuRepository repository;

    @Autowired
    private MyTelegramBot myTelegramBot;


}
