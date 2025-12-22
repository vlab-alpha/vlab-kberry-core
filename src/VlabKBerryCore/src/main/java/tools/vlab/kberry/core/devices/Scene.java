package tools.vlab.kberry.core.devices;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import static tools.vlab.kberry.core.devices.Command.SCENE_ACTIVATION;

public class Scene extends KNXDevice {

    private final Vector<SceneStatus> listener = new Vector<>();
    private final AtomicLong lastExecution = new AtomicLong(0);

    private Scene(PositionPath positionPath) {
        super(positionPath, null, SCENE_ACTIVATION);
    }

    public Scene at(PositionPath positionPath) {
        return new Scene(positionPath);
    }

    public void addListener(SceneStatus listener) {
        this.listener.add(listener);
    }

    public long getLastExecution() {
        return lastExecution.get();
    }

    /**
     * Ruft eine spezifische KNX-Szene auf.
     *
     * @param sceneNumber Die Nummer der Szene (1-64).
     */
    public void activateScene(int sceneNumber) {
        if (sceneNumber >= 1 && sceneNumber <= 64) {
            this.set(Command.SCENE_ACTIVATION, sceneNumber);
        } else {
            throw new InvalidSceneNumberException("Invalid Scene Number: " + sceneNumber + ". Should be between 1 and 64.");
        }
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        if (command == Command.SCENE_ACTIVATION) {
            dataPoint.getUInt8().ifPresent(value -> {
                lastExecution.set(System.currentTimeMillis());
                listener.forEach(sceneStatus -> sceneStatus.onChangedScene(this.getPositionPath(), value));
            });
        }
    }

    @Override
    public void load() {
    }
}
