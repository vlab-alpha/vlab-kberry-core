package tools.vlab.kberry.core.knx.devices.sensor;

import tools.vlab.kberry.core.knx.devices.StatusListener;

public interface HumidityStatus extends StatusListener {

    void humidityChanged(HumiditySensor sensor, float humidity);

}
