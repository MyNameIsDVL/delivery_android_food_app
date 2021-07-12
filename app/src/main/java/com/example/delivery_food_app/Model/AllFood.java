/*
 * Copyright (c) 2021 | Mateusz Hus.
 * The content of this application, including (but not limited to) all written material, images, photos, and code, are protected under international copyright and trademark laws. You may not copy, reproduce, modify, republish, transmit or distribute any material from this application.
 *
 * Author: Mateusz Hus
 *
 */

package com.example.delivery_food_app.Model;

public class AllFood {
    String Name;
    String Price;
    String Rating;
    Integer ImageUrl;

    public AllFood() {}

    public AllFood(String name, String price, String rating, Integer imageUrl) {
        Name = name;
        Price = price;
        Rating = rating;
        ImageUrl = imageUrl;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public Integer getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(Integer imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getRating() {
        return Rating;
    }

    public void setRating(String rating) {
        Rating = rating;
    }
}
