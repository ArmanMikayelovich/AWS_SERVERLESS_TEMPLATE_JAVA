package com.mikayelovich.serverless_app.lambda;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikayelovich.serverless_app.dto.ProductDTO;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Handler for requests to Lambda function.
 */
public class ReadFileLambda {
    private static final String PRODUCTS_QUEUE = System.getenv("PRODUCTS_QUEUE");
    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

    /**
     * Handles S3 bucket events. This method is triggered when an object is put into the S3 bucket.
     * For each record in the event, it retrieves the object from S3, parses it into a list of products,
     * and pushes each product to an SQS queue.
     *
     * @param event The S3 event containing records of objects put into the bucket.
     */
    public void handler(S3Event event) {
        event.getRecords().forEach(s3Record -> {
            S3Object object = s3.getObject(s3Record.getS3().getBucket().getName(), s3Record.getS3().getObject().getKey());
            S3ObjectInputStream s3InputStream = object.getObjectContent();
            try {
                List<ProductDTO> productsData = Arrays.asList(mapper.convertValue(s3InputStream, ProductDTO[].class));
                s3InputStream.close();
                pushMessageToSQS(productsData);
            } catch (IOException e) {
                System.err.println("Error processing S3 object: " + e.getMessage());
            }
        });

    }


    /**
     * Pushes a list of products to an SQS queue.
     *
     * @param productsData The list of products to be pushed to the SQS queue.
     */
    private void pushMessageToSQS(List<ProductDTO> productsData) {
        productsData.forEach(productDTO -> {
            try {
                SendMessageRequest sendMessageRequest = new SendMessageRequest(PRODUCTS_QUEUE,
                        mapper.writeValueAsString(productDTO));
                sqs.sendMessage(sendMessageRequest);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

}











/*    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
            String output = String.format("{ \"message\": \"hello world\", \"location\": \"%s\" }", pageContents);

            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (IOException e) {
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

    private String getPageContents(String address) throws IOException{
        URL url = new URL(address);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }*/
