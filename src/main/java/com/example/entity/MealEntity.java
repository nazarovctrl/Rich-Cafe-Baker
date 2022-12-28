package com.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


@Entity(name = "meals")
public class MealEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column
    private Double price;

    @Column(columnDefinition = "text")
    private String photo;

    @ManyToOne(fetch = FetchType.LAZY)
    private MenuEntity menu;




}
