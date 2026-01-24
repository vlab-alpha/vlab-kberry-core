package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.List;
import java.util.stream.Collectors;

public class Jalousie extends KNXDevice {

    private final PersistentValue<Integer> currentPosition;

    private Jalousie(PositionPath positionPath, Integer refreshData) {
        super(positionPath,
                refreshData,
                Command.SHUTTER_UP_DOWN_CONTROL,
                Command.STOP,
                Command.SHUTTER_REFERENCE,
                Command.SHUTTER_POSITION_SET,
                Command.SHUTTER_POSITION_ACTUAL_STATUS);
        this.currentPosition = new PersistentValue<>(positionPath, "jalousieCurrentPosition", 0, Integer.class);
    }

    public static Jalousie at(PositionPath positionPath) {
        return new Jalousie(positionPath, null);
    }

    public void up() {
        this.set(Command.SHUTTER_UP_DOWN_CONTROL, false);
    }

    public void down() {
        this.set(Command.SHUTTER_UP_DOWN_CONTROL, true);
    }

    public void referenceDriving() {
        this.set(Command.SHUTTER_REFERENCE, true);
    }

    public void stop() {
        this.set(Command.STOP, true);
    }

    public int currentPosition() {
        return this.currentPosition.get();
    }

    public void setPosition(int position) {
        this.set(Command.SHUTTER_POSITION_SET, position);
    }

    public int getCurrentPositionPercent() {
        return Math.round(this.currentPosition.get() * 100f / 255f);
    }

    public void setPositionPercent(int percent) {
        setPosition(Math.round(percent * 255f / 100f));
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case SHUTTER_POSITION_ACTUAL_STATUS -> dataPoint.getUInt8().ifPresent(value -> {
                this.currentPosition.set(value);
                getListener().forEach(status -> status.positionChanged(this, value));
            });
        }
    }

    private List<JalousieStatus> getListener() {
        return this.listeners.stream().filter(l -> l instanceof JalousieStatus).map(l -> (JalousieStatus) l).collect(Collectors.toList());
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.SHUTTER_POSITION_ACTUAL_STATUS).flatMap(DataPoint::getUInt8).ifPresent(currentPosition::set);
    }
}
