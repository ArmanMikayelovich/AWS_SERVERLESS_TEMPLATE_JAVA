package com.mikayelovich.serverless_app.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikayelovich.serverless_app.entity.Product;

import java.util.List;

public class GetAllProductsLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME = System.getenv("PRODUCTS_TABLE");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(TABLE_NAME))
            .build();

    private final DynamoDBMapper mapper = new DynamoDBMapper((AmazonDynamoDBClientBuilder.defaultClient()), mapperConfig);


    private final DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression();


  /**
 * Handles the incoming API Gateway request to retrieve all products from the DynamoDB table.
 *
 * @param apiGatewayProxyRequestEvent The API Gateway request event.
 * @param context The Lambda execution environment context object.
 * @return The API Gateway response event containing the list of products or an error message.
 */
@Override
public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
 List<Product> scanResult = mapper.scan(Product.class, dynamoDBScanExpression);
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.withStatusCode(200);
        try {
            responseEvent.withBody(objectMapper.writeValueAsString(scanResult));
        } catch (JsonProcessingException e) {
            responseEvent.withStatusCode(500);
            responseEvent.withBody("Error processing request: " + e.getMessage());
        }
        return responseEvent;
    }
}
