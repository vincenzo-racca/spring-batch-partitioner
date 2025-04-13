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
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class SpringBatchAwsApplication {


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
				var deliveryDriverId = deliveryDriverProvince[0];
				var province = deliveryDriverProvince[1];
				DeliveryDriverCapacity deliveryDriverCapacity = buildDeliveryDriverCapacity(pk, deliveryDriverId, province);
				dynamoDbTemplate.save(deliveryDriverCapacity);
				for(int i = 0; i < numberOfRequest; i++) {
					PaperDelivery paperDelivery = buildPaperDelivery(pk, deliveryDriverId, province);
					dynamoDbTemplate.save(paperDelivery);
				}
			});

		};
	}

	private static PaperDelivery buildPaperDelivery(String pk, String deliveryDriverId, String province) {
		PaperDelivery paperDelivery = new PaperDelivery();
		paperDelivery.setPk(pk);
		paperDelivery.setCreatedAt(Instant.now());
		paperDelivery.setRequestId(UUID.randomUUID().toString());
		paperDelivery.setDeliveryDriverId(deliveryDriverId);
		paperDelivery.setProvince(province);
		return paperDelivery;
	}

	private static DeliveryDriverCapacity buildDeliveryDriverCapacity(String pk, String deliveryDriverId, String province) {
		DeliveryDriverCapacity deliveryDriverCapacity = new DeliveryDriverCapacity();
		deliveryDriverCapacity.setPk(pk);
		deliveryDriverCapacity.setDeliveryDriverId(deliveryDriverId);
		deliveryDriverCapacity.setProvince(province);
		if(pk.equals("1##RM") || pk.equals("2##NA")) {
			deliveryDriverCapacity.setCapacity(1L);
		}
		else {
			deliveryDriverCapacity.setCapacity(2L);
		}
		deliveryDriverCapacity.setUsedCapacity(0L);
		return deliveryDriverCapacity;
	}

	private <T> void cleanDb(DynamoDbTemplate dynamoDbTemplate, Class<T> clazz) {
		PageIterable<T> pages = dynamoDbTemplate.scanAll(clazz);
		StreamSupport.stream(pages.items().spliterator(), false)
				.forEach(dynamoDbTemplate::delete);
	}



}
