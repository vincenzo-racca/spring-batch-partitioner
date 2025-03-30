package com.vincenzoracca.springbatchpartitioner.job.reader.partitioner;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaperDeliveryPartitioner implements Partitioner {

    private final List<String> pks;

    public PaperDeliveryPartitioner(List<String> pks) {
        this.pks = pks;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();

        for (int i = 0; i < pks.size(); i++) {
            ExecutionContext context = new ExecutionContext();
            context.putString("pk", pks.get(i));
            partitions.put("partition" + i, context);
        }

        return partitions;
    }
}
