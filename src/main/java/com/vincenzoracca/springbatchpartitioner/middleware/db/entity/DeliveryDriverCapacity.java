package com.vincenzoracca.springbatchpartitioner.middleware.db.entity;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
@Data
public class DeliveryDriverCapacity {

    private String pk; // deliveryDriverId##province
    private String deliveryDriverId;
    private String province;
    private long capacity;
    private long usedCapacity;

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }
}
