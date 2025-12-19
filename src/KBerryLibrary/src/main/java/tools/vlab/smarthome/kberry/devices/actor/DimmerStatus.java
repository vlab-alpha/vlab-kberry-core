package tools.vlab.smarthome.kberry.devices.actor;

public interface DimmerStatus {

    void isOnChanged(Dimmer dimmer, boolean onOff);
    void brightnessChanged(Dimmer dimmer, int percent);
}
