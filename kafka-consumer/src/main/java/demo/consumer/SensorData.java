package demo.consumer;



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
    private String deviceId;
    private double accX;
    private double accY;
    private double accZ;
    private double gyroX;
    private double gyroY;
    private double gyroZ;
    private double latitude;
    private double longitude;
    private long timestamp;

    public static SensorData fromJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, SensorData.class);
    }
}
