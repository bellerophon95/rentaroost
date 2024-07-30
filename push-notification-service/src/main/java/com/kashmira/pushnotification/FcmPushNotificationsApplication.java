package com.kashmira.pushnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class FcmPushNotificationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FcmPushNotificationsApplication.class, args);
	}

}
