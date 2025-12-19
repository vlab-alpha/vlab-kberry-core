package tools.vlab.smarthome.kberry.devices.sensor;

public interface TemperatureStatus {

    void temperatureChanged(TemperatureSensor sensor, float celsius);
}
