package tools.vlab.kberry.core.knx.devices.actor;

import tools.vlab.kberry.core.knx.devices.StatusListener;

public interface JalousieStatus extends StatusListener {

    void positionChanged(Jalousie jalousie, int position);
}
