package com.vincenzoracca.springbatchpartitioner;

import com.vincenzoracca.springbatchpartitioner.middleware.db.entity.DeliveryDriverCapacity;
import com.vincenzoracca.springbatchpartitioner.middleware.db.entity.PaperDelivery;
import com.vincenzoracca.springbatchpartitioner.middleware.mock.ExternalService;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class SpringBatchAwsApplication {

	private final Random random = new Random();

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchAwsApplication.class, args);
	}


	@Bean
	CommandLineRunner commandLineRunner(DynamoDbTemplate dynamoDbTemplate) {
		return args -> {
			cleanDb(dynamoDbTemplate, PaperDelivery.class);
			cleanDb(dynamoDbTemplate, DeliveryDriverCapacity.class);

			String[] pks = ExternalService.buildPks().split(",");
			int numberOfRequest = 2;
			log.info("Initialization {} requestId for every {} pair deliveryDriverId##Province", numberOfRequest, pks.length);
			Stream.of(pks).parallel().forEach(pk -> {
				String[] deliveryDriverProvince = pk.split("##");
				DeliveryDriverCapacity deliveryDriverCapacity = new DeliveryDriverCapacity();
				deliveryDriverCapacity.setPk(pk);
				deliveryDriverCapacity.setDeliveryDriverId(deliveryDriverProvince[0]);
				deliveryDriverCapacity.setProvince(deliveryDriverProvince[1]);
				deliveryDriverCapacity.setCapacity(random.nextLong(2) + 1);
				deliveryDriverCapacity.setUsedCapacity(0L);
				dynamoDbTemplate.save(deliveryDriverCapacity);
				for(int i = 0; i < numberOfRequest; i++) {
					PaperDelivery paperDelivery = new PaperDelivery();
					paperDelivery.setPk(pk);
					paperDelivery.setCreatedAt(Instant.now());
					paperDelivery.setRequestId(UUID.randomUUID().toString());
					paperDelivery.setDeliveryDriverId(deliveryDriverProvince[0]);
					paperDelivery.setProvince(deliveryDriverProvince[1]);
					dynamoDbTemplate.save(paperDelivery);
				}
			});

		};
	}

	private <T> void cleanDb(DynamoDbTemplate dynamoDbTemplate, Class<T> clazz) {
		PageIterable<T> pages = dynamoDbTemplate.scanAll(clazz);
		StreamSupport.stream(pages.items().spliterator(), false)
				.forEach(dynamoDbTemplate::delete);
	}



}
