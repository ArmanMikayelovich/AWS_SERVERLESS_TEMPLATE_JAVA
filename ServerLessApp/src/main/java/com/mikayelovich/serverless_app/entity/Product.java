package com.mikayelovich.serverless_app.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Data;

@Data
public class Product {
    private String id;
    private String name;
    private String description;
    private int price;
}
