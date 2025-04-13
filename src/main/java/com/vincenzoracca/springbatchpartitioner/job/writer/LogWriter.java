package com.vincenzoracca.springbatchpartitioner.job.writer;

import com.vincenzoracca.springbatchpartitioner.middleware.db.entity.DeliveryDriverCapacity;
import com.vincenzoracca.springbatchpartitioner.middleware.db.entity.PaperDelivery;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Slf4j
@RequiredArgsConstructor
public class LogWriter implements ItemWriter<PaperDelivery> {

    private final DynamoDbTemplate dynamoDbTemplate;

    @Override
    public void write(Chunk<? extends PaperDelivery> chunk) {
        String pk = chunk.getItems().get(0).getPk();
        DeliveryDriverCapacity deliveryDriverCapacity = dynamoDbTemplate.load(Key.builder()
                .partitionValue(pk)
                .build(), DeliveryDriverCapacity.class);

        chunk.forEach(paper -> {
            if (deliveryDriverCapacity != null &&
                    deliveryDriverCapacity.getUsedCapacity() < deliveryDriverCapacity.getCapacity()) {

                deliveryDriverCapacity.setUsedCapacity(deliveryDriverCapacity.getUsedCapacity() + 1);
                log.info("Paper Delivery DONE: {}",paper);
                dynamoDbTemplate.delete(paper);
            } else {
                log.warn("PaperDelivery DISCARDED because there is no delivery driver capacity: {}, {}", paper.getRequestId(), paper.getPk());
            }
        });

        dynamoDbTemplate.save(deliveryDriverCapacity);
    }

}
