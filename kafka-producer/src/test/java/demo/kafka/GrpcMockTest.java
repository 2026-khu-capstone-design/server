package demo.kafka;

import demo.kafka.grpc.SensorRequest;
import demo.kafka.grpc.SensorResponse;
import demo.kafka.grpc.SensorServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

public class GrpcMockTest {
    @Test
    void simulatePhoneData() throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        // 스트리밍 방식은 Stub
        SensorServiceGrpc.SensorServiceStub stub = SensorServiceGrpc.newStub(channel);

        // 서버로부터 응답을 받기 위한 Observer (결과 확인용)
        StreamObserver<SensorResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(SensorResponse value) {
                System.out.println("✅ 서버 응답: " + value.getStatus());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println(" 에러 발생: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println(" 스트리밍 종료");
            }
        };

        //  서버로 데이터를 보낼 스트림을 엽니다.
        StreamObserver<SensorRequest> requestObserver = stub.collectSensorData(responseObserver);

        for (int i = 0; i < 10; i++) {
            SensorRequest request = SensorRequest.newBuilder()
                    .setDeviceId("Phone_Mock_" + (i % 10))
                    .setAccX(Math.random() * 25.0)
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            // 데이터를 스트림에 실어서 보냅니다.
            requestObserver.onNext(request);
            System.out.println("🚀 데이터 스트리밍 중: " + request.getDeviceId());

            Thread.sleep(500); // 0.5초 간격으로 전송
        }

        // 전송 완료 알림
        requestObserver.onCompleted();

        // 응답을 기다리기 위해 잠시 대기
        Thread.sleep(2000);
        channel.shutdown();

    }
}
