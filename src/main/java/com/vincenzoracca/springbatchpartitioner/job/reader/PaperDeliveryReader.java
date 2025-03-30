package com.vincenzoracca.springbatchpartitioner.job.reader;

import com.vincenzoracca.springbatchpartitioner.middleware.db.entity.PaperDelivery;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.*;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public class PaperDeliveryReader implements ItemReader<PaperDelivery>, StepExecutionListener {

    private final DynamoDbTemplate dynamoDbTemplate;
    private Iterator<PaperDelivery> iterator;


    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext context = stepExecution.getExecutionContext();
        var pk = context.getString("pk");
        this.iterator = loadData(pk).iterator();
    }

    private List<PaperDelivery> loadData(String pk) {
        QueryEnhancedRequest query = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(pk))).build();
        PageIterable<PaperDelivery> pageIterable = dynamoDbTemplate.query(query, PaperDelivery.class);
        return StreamSupport.stream(pageIterable.items().spliterator(), false).toList();
    }

    @Override
    public PaperDelivery read() {
        return (iterator != null && iterator.hasNext()) ? iterator.next() : null;
    }
}
