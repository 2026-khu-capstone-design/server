package demo.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SensorConsumerService {
    @KafkaListener(topics = "sensor.stream",groupId = "analytics-group")
    public void consumeForAnalysis(String message){
        log.info("consumer 실시간 분석");
    }

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 100,multiplier = 4),
            dltTopicSuffix = ".storage.dlt"
    )
    @KafkaListener(topics = "sensor.stream",groupId = "storage-group")
    public void consumeForStorage(String message){
        log.info("consume 데이터 저장");
    }
}
