package tools.vlab.kberry.core.mqtt.shelly.devices.device;

import tools.vlab.kberry.core.RGBW;

public interface LedStatus {

    void isOnChanged(Led led, boolean isOn);
    void colorChanged(Led led, RGBW rgb);
    void brightnessChanged(Led led, int brightness);

}
