package com.mikayelovich.serverless_app.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private String name;
    private String description;
    private int price;
}
