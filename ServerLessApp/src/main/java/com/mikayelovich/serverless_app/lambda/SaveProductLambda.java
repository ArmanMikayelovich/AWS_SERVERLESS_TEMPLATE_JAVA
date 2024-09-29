package com.mikayelovich.serverless_app.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikayelovich.serverless_app.dto.ProductDTO;

import java.util.UUID;

/**
 * Lambda function to save product data in DynamoDB.
 */
public class SaveProductLambda {

    private static final String TABLE_NAME = System.getenv("PRODUCTS_TABLE");
    private final ObjectMapper mapper = new ObjectMapper();

    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());

    /**
     * Handles SQS events. This method is triggered when a message is received in the SQS queue.
     * For each record in the event, it retrieves the message body, maps it to a ProductDTO object,
     * and saves the product data into a DynamoDB table.
     *
     * @param event The SQS event containing records of messages received in the queue.
     */
    public void handler(SQSEvent event) {
        event.getRecords().forEach(record -> {
            try {
                ProductDTO productDTO = mapper.readValue(record.getBody(), ProductDTO.class);
                Table table = dynamoDB.getTable(TABLE_NAME);
                Item item = new Item()
                        .withPrimaryKey("id", UUID.randomUUID().toString())
                        .withString("name", productDTO.getName())
                        .withString("description", productDTO.getDescription())
                        .withInt("price", productDTO.getPrice());
                table.putItem(item);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error mapping JSON to ProductDTO", e);
            }
        });
    }
}