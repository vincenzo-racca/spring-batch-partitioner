package com.vincenzoracca.springbatchpartitioner.middleware.db.entity;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;

@DynamoDbBean
@Data
public class PaperDelivery {

    private String pk; // deliveryDriverId##province
    private Instant createdAt;
    private String deliveryDriverId;
    private String province;
    private String requestId;

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    @DynamoDbSortKey
    public Instant getCreatedAt() {
        return createdAt;
    }
}
