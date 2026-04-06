package demo.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

@Service
@Slf4j
@RequiredArgsConstructor
public class SensorConsumerService {

    private final SimpMessagingTemplate messagingTemplate;

    // group 1 : 실시간 시각화
    @KafkaListener(topics = "sensor.stream", groupId = "display-group", concurrency = "2")
    public void consumeForDisplay(String message,@Header(KafkaHeaders.RECEIVED_PARTITION) int partition){
        log.info("[Display-Group] 파티션: {}, 메시지: {}", partition, message);
       messagingTemplate.convertAndSend("/topic/sensors", message);
    }

    // group2 : 이상치 알림 전용
    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 100,multiplier = 4),
            dltTopicSuffix = ".storage.dlt"
    )
    @KafkaListener(topics = "sensor.stream", groupId = "alert-group", concurrency = "2")
    public void consumeForAlert(String message){
        try {
            Thread.sleep(200);

            SensorData data = SensorData.fromJson(message);
            // 로직: 가속도 합산 벡터가 특정 값 이상이면 위험으로 판단
            double totalAcc = Math.sqrt(Math.pow(data.getAccX(), 2)
                    + Math.pow(data.getAccY(), 2)
                    + Math.pow(data.getAccZ(), 2));

            if (totalAcc > 20.0) {
                log.warn(" 기기 {}에서 강한 충격 감지: {}", data.getDeviceId(), totalAcc);

                // 알림 전용 토픽으로 별도 전송
                messagingTemplate.convertAndSend("/topic/alerts",
                        "Device " + data.getDeviceId() + ": 위험감지");
            }
        } catch (Exception e) {
            log.error("알림 분석 에러 발생: {}", e.getMessage());
        }

    }
}
