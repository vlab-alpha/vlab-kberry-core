package tools.vlab.smarthome.kberry.devices;

import tools.vlab.smarthome.kberry.PositionPath;

public interface PushButtonStatus {

    void enableChanged(PositionPath positionPath, boolean enable);
    void pushButtonStatusChanged(PositionPath positionPath, boolean onOff);
}
