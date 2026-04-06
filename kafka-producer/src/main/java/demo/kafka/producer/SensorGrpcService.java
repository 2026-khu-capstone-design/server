package demo.kafka.producer;

import demo.kafka.grpc.SensorRequest;
import demo.kafka.grpc.SensorResponse;
import demo.kafka.grpc.SensorServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.kafka.core.KafkaTemplate;
import com.google.protobuf.util.JsonFormat;
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class SensorGrpcService extends SensorServiceGrpc.SensorServiceImplBase{
    private final KafkaTemplate<String,String> kafkaTemplate;
    private static final String TOPIC ="sensor.stream";

    @Override
    public StreamObserver<SensorRequest> collectSensorData(StreamObserver<SensorResponse> responseObserver) {
        return new StreamObserver<SensorRequest>() {
            @Override
            public void onNext(SensorRequest sensorRequest) {
                // 데이터가 올 때 마다 실행 -> json 변환 및 카프카 전송
                try {
                    String jsonMessage = JsonFormat.printer().print(sensorRequest);
                    kafkaTemplate.send(TOPIC,sensorRequest.getDeviceId(),jsonMessage);
                    log.info("stream 수신 devide:{} kafka 전송 완",sensorRequest.getDeviceId());
                }catch (Exception e){
                    log.error("json 변환 실패 ");
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error(" gRPC 스트림 에러 발생: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                log.info("클라이언트 데이터 전송 완료");
                SensorResponse response = SensorResponse.newBuilder()
                        .setStatus(200)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }


}
