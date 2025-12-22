package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.Objects;
import java.util.Vector;

import static tools.vlab.kberry.core.devices.Command.VOC_ACTUAL;

public class VOCSensor extends KNXDevice {

    private final Vector<VOCStatus> listener = new Vector<>();
    private final PersistentValue<Float> currentVoc;

    private VOCSensor(PositionPath positionPath,Integer refreshData) {
        super(positionPath, refreshData, VOC_ACTUAL);
        this.currentVoc = new PersistentValue<>(positionPath, "VOC", 0.0f, Float.class);
    }

    public static VOCSensor at(PositionPath positionPath) {
        return new VOCSensor(positionPath, null);
    }

    public static VOCSensor at(PositionPath positionPath, int refreshIntervallMs) {
        return new VOCSensor(positionPath, refreshIntervallMs);
    }

    public void addListener(VOCStatus listener) {
        this.listener.add(listener);
    }

    public float getCurrentPPM() {
        return currentVoc.get();
    }

    @Override
    public void received(Command command, DataPoint dataPoint) {
        if (Objects.requireNonNull(command) == VOC_ACTUAL) {
            dataPoint.getFloat9().ifPresent(value -> {
                currentVoc.set(value);
                listener.forEach(vocStatus -> vocStatus.vocChanged(this, value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(VOC_ACTUAL).flatMap(DataPoint::getFloat9).ifPresent(currentVoc::set);
    }
}
