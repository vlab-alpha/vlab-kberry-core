package tools.vlab.kberry.core.devices.sensor;

public interface TemperatureStatus {

    void temperatureChanged(TemperatureSensor sensor, float celsius);
}
