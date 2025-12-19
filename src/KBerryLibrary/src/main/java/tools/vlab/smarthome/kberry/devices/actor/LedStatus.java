package tools.vlab.smarthome.kberry.devices.actor;

import tools.vlab.smarthome.kberry.devices.RGB;

public interface LedStatus {

    void colorChanged(Led led, RGB color);

    void isOnChanged(Led led, boolean onOff);
}
