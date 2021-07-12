/*
 * Copyright (c) 2021 | Mateusz Hus.
 * The content of this application, including (but not limited to) all written material, images, photos, and code, are protected under international copyright and trademark laws. You may not copy, reproduce, modify, republish, transmit or distribute any material from this application.
 *
 * Author: Mateusz Hus
 *
 */

package com.example.delivery_food_app.Model;

public class Basket {
    String IdUser;
    String ProductCategory;
    String ProductDescription;
    String ProductId;
    String ProductName;
    String ProductPrice;
    String ProductQuantity;
    String IdKey;

    public Basket() {}

    public Basket(String idUser, String productCategory, String productDescription, String productId, String productName, String productPrice, String productQuantity, String idKey) {
        IdUser = idUser;
        ProductCategory = productCategory;
        ProductDescription = productDescription;
        ProductId = productId;
        ProductName = productName;
        ProductPrice = productPrice;
        ProductQuantity = productQuantity;
        IdKey = idKey;
    }

    public String getIdUser() {
        return IdUser;
    }

    public void setIdUser(String idUser) {
        IdUser = idUser;
    }

    public String getProductCategory() {
        return ProductCategory;
    }

    public void setProductCategory(String productCategory) {
        ProductCategory = productCategory;
    }

    public String getProductDescription() {
        return ProductDescription;
    }

    public void setProductDescription(String productDescription) {
        ProductDescription = productDescription;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductPrice() {
        return ProductPrice;
    }

    public void setProductPrice(String productPrice) {
        ProductPrice = productPrice;
    }

    public String getProductQuantity() {
        return ProductQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        ProductQuantity = productQuantity;
    }

    public String getIdKey() {
        return IdKey;
    }

    public void setIdKey(String idKey) {
        IdKey = idKey;
    }
}
