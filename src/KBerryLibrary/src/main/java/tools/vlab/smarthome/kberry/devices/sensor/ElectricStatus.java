package tools.vlab.smarthome.kberry.devices.sensor;

public interface ElectricStatus {

    void kwhChanged(ElectricitySensor sensor, float kwh);

    void electricityChanged(ElectricitySensor sensor, int electricity);

}
