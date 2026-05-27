package demo.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.messaging.handler.annotation.Header;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j
@RequiredArgsConstructor
public class SensorConsumerService {

    private final SimpMessagingTemplate messagingTemplate;
    // 최근 50개 데이터 저장소
    private final Map<String, Deque<SensorData>> deviceHistory = new ConcurrentHashMap<>();

    // group 1 : 실시간 시각화
    @KafkaListener(topics = "sensor.stream", groupId = "display-group", concurrency = "${kafka.concurrency}")
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

            Deque<SensorData> history = deviceHistory.computeIfAbsent(
                    data.getDeviceId(),k ->new ArrayDeque<>()
            );
            history.addLast(data);
            if (history.size() > 50) history.pollFirst();

            //평균 가속도
            double avgAcc = history.stream()
                .mapToDouble(d -> Math.sqrt(
                        Math.pow(d.getAccX(), 2) +
                                Math.pow(d.getAccY(), 2) +
                                Math.pow(d.getAccZ(), 2)))
                .average()
                .orElse(0);

            //분산 - 가속도가 평균에서 얼마나 흩어진지
            // 분산큼 - 이상움직임 가능성
            double variance = history.stream()
                .mapToDouble(d -> Math.pow(
                        Math.sqrt(
                                Math.pow(d.getAccX(), 2) +
                                        Math.pow(d.getAccY(), 2) +
                                        Math.pow(d.getAccZ(), 2)
                        ) - avgAcc, 2))
                .average()
                .orElse(0);

            //현재 프레임 벡터 계산 - 순간의 가속도/자이로 크기
            double totalAcc = Math.sqrt(
                Math.pow(data.getAccX(), 2) +
                        Math.pow(data.getAccY(), 2) +
                        Math.pow(data.getAccZ(), 2));

            double totalGyro = Math.sqrt(
                Math.pow(data.getGyroX(), 2) +
                        Math.pow(data.getGyroY(), 2) +
                        Math.pow(data.getGyroZ(), 2));

            boolean isShock = totalAcc > 20.0;        // 강한 충격
            boolean isRotation = totalGyro > 5.0;     // 빠른 회전
            boolean isAbnormal = variance > 10.0;     // 불규칙한 움직임

            if (isShock && isRotation) {
                log.warn("낙상 감지 - 기기: {}, 충격: {}, 회전: {}", data.getDeviceId(), totalAcc, totalGyro);
                messagingTemplate.convertAndSend("/topic/alerts",
                    "Device " + data.getDeviceId() + ": 낙상 감지");
            }

            // 이상 움직임 감지: 분산이 임계값 초과
            if (isAbnormal) {
                log.warn("이상 움직임 - 기기: {}, 분산: {}, 평균가속도: {}", data.getDeviceId(), variance, avgAcc);
                messagingTemplate.convertAndSend("/topic/alerts",
                    "Device " + data.getDeviceId() + ": 이상 움직임 감지");
            }
        }
    }