package com.example.entity;

import com.example.enums.MethodType;
import com.example.enums.OrdersStatus;
import com.example.enums.Payment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrdersEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column
    private OrdersStatus status;

    @Column
    private LocalDateTime createdDate;

//    @ManyToOne
//    @JoinColumn(name = "meal_table")
//    private MealEntity meal_table;


    @ManyToOne
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @Enumerated(EnumType.STRING)
    @Column
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_type")
    private MethodType methodType;


    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private AdminEntity supplier;

    @Column
    private Boolean visible = true;


    @Column
    private Double longitude;

    @Column
    private Double latitude;
}
