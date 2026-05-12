package tools.vlab.kberry.core.knx.devices.sensor;

import tools.vlab.kberry.core.knx.devices.StatusListener;

public interface VOCStatus extends StatusListener {

    void vocChanged(VOCSensor sensor, float voc);

}
