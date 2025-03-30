#!/bin/bash


echo "Create paper_delivery DynamoDB table"
aws dynamodb create-table \
    --table-name paper_delivery \
    --attribute-definitions \
        AttributeName=pk,AttributeType=S \
        AttributeName=createdAt,AttributeType=S \
        AttributeName=requestId,AttributeType=S \
    --key-schema \
        AttributeName=pk,KeyType=HASH \
        AttributeName=createdAt,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --global-secondary-indexes \
            "[
                {
                    \"IndexName\": \"RequestIdIndex\",
                    \"KeySchema\": [{\"AttributeName\":\"requestId\",\"KeyType\":\"HASH\"}],
                    \"Projection\":{
                        \"ProjectionType\":\"ALL\"
                    },
                    \"ProvisionedThroughput\": {
                        \"ReadCapacityUnits\": 5,
                        \"WriteCapacityUnits\": 5
                    }
                }
            ]" \
    --endpoint-url http://localhost:4566

echo "Create delivery_driver_capacity DynamoDB table"
aws dynamodb create-table \
    --table-name delivery_driver_capacity \
    --attribute-definitions \
        AttributeName=pk,AttributeType=S \
    --key-schema \
        AttributeName=pk,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:4566
