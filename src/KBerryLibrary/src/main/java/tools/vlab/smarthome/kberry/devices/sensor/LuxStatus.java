package tools.vlab.smarthome.kberry.devices.sensor;

public interface LuxStatus {

    void luxChanged(LuxSensor sensor, float lux);
}
