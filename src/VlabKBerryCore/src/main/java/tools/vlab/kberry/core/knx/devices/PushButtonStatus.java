package tools.vlab.kberry.core.knx.devices;

import tools.vlab.kberry.core.PositionPath;

public interface PushButtonStatus {

    void enableChanged(PositionPath positionPath, boolean enable);
}
