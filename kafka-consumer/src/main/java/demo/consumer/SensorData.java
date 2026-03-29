package demo.consumer;


import demo.test.EmailSendMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorData {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String deviceId; // 센서 고유 ID;

    //가속소 센서
    private double accX;
    private double accY;
    private double accZ;

    //자이로스코프 (rad/s)
    private double gyroX;
    private double gyroY;
    private double gyroZ;

    //gps
    private double latitude;
    private double longitude;

    private long timestamp;

    public static SensorData fromJson(String json){
        try{
            return objectMapper.readValue(json,SensorData.class);
        }catch (JacksonException e){
            throw new RuntimeException("json 파싱 실패");
        }
    }
}
