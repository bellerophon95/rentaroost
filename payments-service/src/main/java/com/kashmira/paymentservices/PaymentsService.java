package com.kashmira.paymentservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;


@SpringBootApplication
@EnableKafka
public class  PaymentsService {


    public static void main(String[] args)  {
        SpringApplication.run(PaymentsService.class, args);
    }

}
