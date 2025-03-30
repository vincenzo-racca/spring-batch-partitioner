package com.vincenzoracca.springbatchpartitioner.job.config;

import com.vincenzoracca.springbatchpartitioner.job.reader.PaperDeliveryReader;
import com.vincenzoracca.springbatchpartitioner.job.reader.partitioner.PaperDeliveryPartitioner;
import com.vincenzoracca.springbatchpartitioner.job.writer.LogWriter;
import com.vincenzoracca.springbatchpartitioner.middleware.db.entity.PaperDelivery;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
public class PaperDeliveryJobConfig {


    private final DynamoDbTemplate dynamoDbTemplate;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;


    private TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("part-thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    @StepScope
    public PaperDeliveryReader reader() {
        return new PaperDeliveryReader(dynamoDbTemplate);
    }

    @Bean
    public ItemWriter<PaperDelivery> writer() {
        return new LogWriter(dynamoDbTemplate);
    }


    @Bean
    public Step slaveStep() {
        return new StepBuilder("slaveStep", jobRepository)
                .<PaperDelivery, PaperDelivery>chunk(10, transactionManager)
                .reader(reader())
                .writer(writer())
                .build();
    }

    @Bean
    @JobScope
    public Step masterStep(@Value("#{jobParameters[pks]}") String pkParam) {

        List<String> pks = Arrays.asList(pkParam.split(","));

        return new StepBuilder("masterStep", jobRepository)
                .partitioner("slaveStep", new PaperDeliveryPartitioner(pks))
                .step(slaveStep())
                .taskExecutor(taskExecutor())
                .gridSize(pks.size())
                .build();
    }

    @Bean
    public Job paperDeliveryJob(Step masterStep) {
        return new JobBuilder("paperDeliveryJob", jobRepository)
                .start(masterStep)
                .build();
    }


}
