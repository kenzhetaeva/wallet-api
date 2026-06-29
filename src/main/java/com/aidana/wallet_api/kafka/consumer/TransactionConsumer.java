package com.aidana.wallet_api.kafka.consumer;

import com.aidana.wallet_api.kafka.event.TransactionEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionConsumer {

    @KafkaListener(topics = "transactions", groupId = "wallet-api")
    public void consume(TransactionEvent event) {
        System.out.println(event);
    }
}
