package tools.vlab.kberry.core.devices;

import tools.vlab.kberry.core.PositionPath;

public interface PushButtonStatus {

    void enableChanged(PositionPath positionPath, boolean enable);
    void pushButtonStatusChanged(PositionPath positionPath, boolean onOff);
}
