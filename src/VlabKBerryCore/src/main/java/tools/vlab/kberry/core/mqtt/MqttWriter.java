package tools.vlab.kberry.core.mqtt;

public interface MqttWriter<TCommand, TDataPoint> {
    void write(String deviceId, TCommand command, TDataPoint dataPoint);
    void write(String deviceId, TCommand command);
}
