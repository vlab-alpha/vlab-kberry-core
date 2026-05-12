package tools.vlab.kberry.core.knx.devices.actor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.knx.baos.BAOSReadException;
import tools.vlab.kberry.core.knx.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.knx.devices.KnxCommand;
import tools.vlab.kberry.core.knx.devices.KNXDevice;
import tools.vlab.kberry.core.PersistentValue;

import java.util.List;
import java.util.stream.Collectors;

public class Jalousie extends KNXDevice {

    private final PersistentValue<Integer> currentPosition;

    private Jalousie(PositionPath positionPath, Integer refreshData) {
        super(positionPath,
                refreshData,
                KnxCommand.SHUTTER_UP_DOWN_CONTROL,
                KnxCommand.STOP,
                KnxCommand.SHUTTER_REFERENCE,
                KnxCommand.SHUTTER_POSITION_SET,
                KnxCommand.SHUTTER_POSITION_ACTUAL_STATUS);
        this.currentPosition = new PersistentValue<>(positionPath, "jalousieCurrentPosition", 0, Integer.class);
    }

    public static Jalousie at(PositionPath positionPath) {
        return new Jalousie(positionPath, null);
    }

    public void up() {
        this.set(KnxCommand.SHUTTER_UP_DOWN_CONTROL, false);
    }

    public void down() {
        this.set(KnxCommand.SHUTTER_UP_DOWN_CONTROL, true);
    }

    public void referenceDriving() {
        this.set(KnxCommand.SHUTTER_REFERENCE, true);
    }

    public void stop() {
        this.set(KnxCommand.STOP, true);
    }

    public int getCurrentPosition() {
        return this.currentPosition.get();
    }

    public void setPosition(int position) {
        this.set(KnxCommand.SHUTTER_POSITION_SET, position);
    }

    public int getCurrentPositionPercent() {
        return Math.round(this.currentPosition.get() * 100f / 255f);
    }

    public void setPositionPercent(int percent) {
        setPosition(Math.round(percent * 255f / 100f));
    }

    public boolean isDown() {
        return this.currentPosition.get() == 0;
    }

    public boolean isUp() {
        return this.currentPosition.get() == 255;
    }

    @Override
    protected void received(KnxCommand command, DataPoint dataPoint) {
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
        this.get(KnxCommand.SHUTTER_POSITION_ACTUAL_STATUS).flatMap(DataPoint::getUInt8).ifPresent(currentPosition::set);
    }
}
