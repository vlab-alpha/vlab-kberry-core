package tools.vlab.kberry.core.mqtt.custom.devices;

public interface MqttCommandWriter {

    void write(String id, CustomCommand command, CustomDataPoint dataPoint);
    void write(String id, CustomCommand command);
}
