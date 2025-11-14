package com.example.simpleWebApp.service;

import com.example.simpleWebApp.model.Product;

import java.util.Arrays;
import java.util.List;

public class ProductService {

    List<Product> products = Arrays.asList(
            new Product(103, "Iphone", 50000),
            new Product(104, "MacBook", 10000));

   public List<Product> getProducts(){
        return null;
    }
}
