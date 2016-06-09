package com.ironyard;

/**
 * Created by illladell on 6/9/16.
 */
public class Sneaker {
    int id;
    String brand;
    String name;
    int year;
    float price;
    int size;
    String username;


    public Sneaker(int id, String brand, String name, int year, float price, int size, String username) {
        this.id = id;
        this.brand = brand;
        this.name = name;
        this.year = year;
        this.price = price;
        this.size = size;
        this.username = username;
    }
}
