package demo.kafka.producer;

import demo.kafka.producer.DTO.SensorData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/sensor")
@RequiredArgsConstructor
public class SensorController {
   private final SensorProducer sensorProducer;

    @PostMapping("/collect")
    public ResponseEntity<String> collect (@RequestBody SensorData data){
        // key 설정 - 데이터 순서 보장
        sensorProducer.sendMessage(data);
        return ResponseEntity.ok("Data Streamed to Kafka");
    }
}
