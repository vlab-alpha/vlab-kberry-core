package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.devices.RGB;

public interface LedStatus {

    void colorChanged(Led led, RGB color);

    void isOnChanged(Led led, boolean onOff);
}
