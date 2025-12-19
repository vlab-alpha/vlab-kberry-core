package tools.vlab.smarthome.kberry.devices.sensor;

import tools.vlab.smarthome.kberry.AtomicFloat;
import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.KNXDevice;

import java.util.Objects;
import java.util.Vector;

import static tools.vlab.smarthome.kberry.devices.Command.VOC_ACTUAL;

public class VOCSensor extends KNXDevice {

    private final Vector<VOCStatus> listener = new Vector<>();
    private final AtomicFloat currentVoc = new AtomicFloat();

    private VOCSensor(PositionPath positionPath) {
        super(positionPath, VOC_ACTUAL);
    }

    public static VOCSensor at(PositionPath positionPath) {
        return new VOCSensor(positionPath);
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
