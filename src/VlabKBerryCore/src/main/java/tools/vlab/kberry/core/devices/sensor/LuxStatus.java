package tools.vlab.kberry.core.devices.sensor;

public interface LuxStatus {

    void luxChanged(LuxSensor sensor, float lux);
}
