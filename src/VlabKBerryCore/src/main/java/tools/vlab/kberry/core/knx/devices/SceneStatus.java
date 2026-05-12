package tools.vlab.kberry.core.knx.devices;

import tools.vlab.kberry.core.PositionPath;

public interface SceneStatus {

    void onChangedScene(PositionPath positionPath, int sceneNumber);
}
