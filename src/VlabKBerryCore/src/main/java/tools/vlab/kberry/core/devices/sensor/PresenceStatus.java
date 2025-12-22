package tools.vlab.kberry.core.devices.sensor;

public interface PresenceStatus {

    void presenceChanged(PresenceSensor sensor, boolean available);
}
