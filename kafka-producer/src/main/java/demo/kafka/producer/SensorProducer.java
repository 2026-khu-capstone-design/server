package demo.kafka.producer;

import demo.kafka.producer.DTO.SensorData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorProducer {
    private final KafkaTemplate<String,String> kafkaTemplate;
    private static final String TOPIC ="sensor.stream";

    public void sendMessage(SensorData data){
        //key : deviceId 같은 기기는 - 같은 파티션으로 가서 순서보장
        //value :실제 데이터 - json문자열로 일단 작성
        kafkaTemplate.send(TOPIC,data.getDeviceId(),data.toJson());
        log.info("sent message : "+data.getDeviceId());
    }
}
