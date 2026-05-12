package tools.vlab.kberry.core.knx.devices.actor;

import tools.vlab.kberry.core.RGB;
import tools.vlab.kberry.core.knx.devices.StatusListener;

public interface LedStatus extends StatusListener {

    void colorChanged(Led led, RGB color);

    void isOnChanged(Led led, boolean onOff);
}
