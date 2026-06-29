package com.aidana.wallet_api.kafka.producer;

import com.aidana.wallet_api.kafka.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public void send(TransactionEvent event) {
        kafkaTemplate.send("transactions", event);
    }
}
