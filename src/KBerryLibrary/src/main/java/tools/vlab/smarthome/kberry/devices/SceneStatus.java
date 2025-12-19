package tools.vlab.smarthome.kberry.devices;

import tools.vlab.smarthome.kberry.PositionPath;

public interface SceneStatus {

    void onChangedScene(PositionPath positionPath, int sceneNumber);
}
