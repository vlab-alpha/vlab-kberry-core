package tools.vlab.kberry.core.mqtt.shelly.devices;

public interface ShellyWriter {
    void write(String id, ShellyCommand command, ShellyDataPoint dataPoint);
    void write(String id, ShellyCommand command);
}
