package com.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "order_meal")
@Entity
public class OrderMealEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "order_id")
    private Integer orderId;
    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private OrdersEntity order;


    @ManyToOne
    @JoinColumn(name = "meal_id")
    private MealEntity meal;

    @Column
    private Integer quantity;


    @Column
    private Boolean visible = true;
}


