package demo.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
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
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            dltTopicSuffix = ".dlt"
    )

    @KafkaListener(topics = "sensor.stream", groupId = "alert-group", concurrency = "${kafka.concurrency}")
    public void consumeForAlert(String message) throws Exception{

            SensorData data = SensorData.fromJson(message);
            // 로직: 가속도 합산 벡터가 특정 값 이상이면 위험으로 판단
            double totalAcc = Math.sqrt(
                    Math.pow(data.getAccX(), 2)
                            + Math.pow(data.getAccY(), 2)
                            + Math.pow(data.getAccZ(), 2)
            );
            double totalGyro = Math.sqrt(
                    Math.pow(data.getGyroX(), 2)
                            + Math.pow(data.getGyroY(), 2)
                            + Math.pow(data.getGyroZ(), 2)
            );
            boolean isShock = totalAcc > 20.0;
            boolean isRotation = totalGyro > 5.0;

            if (isShock && isRotation) {
                log.warn(" 낙상 감지 - 기기: {}, 충격: {}, 회전: {}", data.getDeviceId(), totalAcc, totalGyro);
                messagingTemplate.convertAndSend("/topic/alerts",
                        "Device " + data.getDeviceId() + ": 낙상 감지");
            }
        }
    }