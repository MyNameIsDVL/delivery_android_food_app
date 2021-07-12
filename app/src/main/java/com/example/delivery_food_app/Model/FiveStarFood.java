/*
 * Copyright (c) 2021 | Mateusz Hus.
 * The content of this application, including (but not limited to) all written material, images, photos, and code, are protected under international copyright and trademark laws. You may not copy, reproduce, modify, republish, transmit or distribute any material from this application.
 *
 * Author: Mateusz Hus
 *
 */

package com.example.delivery_food_app.Model;

public class FiveStarFood {
    String Id;
    String ProductName;
    String ProductDescription;
    String ProductPrice;
    String ProductFiveStar;
    String ProductCategory;

    public FiveStarFood() {}

    public FiveStarFood(String id, String productName, String productDescription, String productPrice, String productFiveStar, String productCategory) {
        Id = id;
        ProductName = productName;
        ProductDescription = productDescription;
        ProductPrice = productPrice;
        ProductFiveStar = productFiveStar;
        ProductCategory = productCategory;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductDescription() {
        return ProductDescription;
    }

    public void setProductDescription(String productDescription) {
        ProductDescription = productDescription;
    }

    public String getProductPrice() {
        return ProductPrice;
    }

    public void setProductPrice(String productPrice) {
        ProductPrice = productPrice;
    }

    public String getProductFiveStar() {
        return ProductFiveStar;
    }

    public void setProductFiveStar(String productFiveStar) {
        ProductFiveStar = productFiveStar;
    }

    public String getProductCategory() {
        return ProductCategory;
    }

    public void setProductCategory(String productCategory) {
        ProductCategory = productCategory;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
}
