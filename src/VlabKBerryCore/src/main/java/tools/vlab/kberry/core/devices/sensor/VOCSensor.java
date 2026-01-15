package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static tools.vlab.kberry.core.devices.Command.VOC_ACTUAL;

public class VOCSensor extends KNXDevice {

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

    public float getCurrentPPM() {
        return currentVoc.get();
    }

    @Override
    public void received(Command command, DataPoint dataPoint) {
        if (Objects.requireNonNull(command) == VOC_ACTUAL) {
            dataPoint.getFloat9().ifPresent(value -> {
                currentVoc.set(value);
                getListener().forEach(vocStatus -> vocStatus.vocChanged(this, value));
            });
        }
    }

    private List<VOCStatus> getListener() {
        return this.listeners.stream().filter(l -> l instanceof VOCStatus).map(l -> (VOCStatus) l).collect(Collectors.toList());
    }

    @Override
    public void load() throws BAOSReadException {
        if (this.currentVoc.isOlderThan(1000 * 60)) {
            this.get(VOC_ACTUAL).flatMap(DataPoint::getFloat9).ifPresent(currentVoc::set);
        }
    }
}
