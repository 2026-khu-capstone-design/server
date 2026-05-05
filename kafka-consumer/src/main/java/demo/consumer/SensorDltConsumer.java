package demo.consumer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SensorDltConsumer {
    @KafkaListener(topics = "sensor.stream.dlt",groupId ="dlt-group" )
    public void processDltMessage(String message, @Header(KafkaHeaders.ORIGINAL_TOPIC) String originTopic) {
        log.error("❌ [DLT 수신] 원본 토픽: {}, 메시지 내용: {}", originTopic, message);
    }
}
