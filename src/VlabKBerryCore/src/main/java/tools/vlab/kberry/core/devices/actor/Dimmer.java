package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.List;
import java.util.stream.Collectors;

public class Dimmer extends KNXDevice {

    private final PersistentValue<Integer> currentBrightness;
    private final PersistentValue<Boolean> currentStatus;

    private Dimmer(PositionPath positionPath,Integer refreshData) {
        super(positionPath,
                refreshData,
                Command.ON_OFF_SWITCH,
                Command.SET_BRIGHTNESS_ABS,
                Command.ON_OFF_STATUS,
                Command.BRIGHTNESS_STATUS_ABS
        );
        this.currentBrightness = new PersistentValue<>(positionPath, "currentBrightness", 0, Integer.class);
        this.currentStatus = new PersistentValue<>(positionPath, "currentStatus", false, Boolean.class);
    }

    public static Dimmer at(PositionPath positionPath) {
        return new Dimmer(positionPath, null);
    }

    public void turnOn() {
        this.set(Command.ON_OFF_SWITCH, true);
    }

    public void turnOff() {
        this.set(Command.ON_OFF_SWITCH, false);
    }

    public boolean isOn() {
        return this.currentStatus.get();
    }

    public int getCurrentBrightness() {
        return this.currentBrightness.get();
    }

    public void setBrightness(int percent) {
        this.set(Command.SET_BRIGHTNESS_ABS, percent);
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case ON_OFF_STATUS -> dataPoint.getBoolean().ifPresent(value -> {
                currentStatus.set(value);
                getListener().forEach(status -> status.isOnChanged(this, value));

            });
            case BRIGHTNESS_STATUS_ABS -> dataPoint.getUInt8().ifPresent(value -> {
                currentBrightness.set(value);
                getListener().forEach(status -> status.brightnessChanged(this, value));
            });
        }
    }

    private List<DimmerStatus> getListener() {
        return this.listeners.stream().filter(l -> l instanceof DimmerStatus).map(l -> (DimmerStatus) l).collect(Collectors.toList());
    }


    @Override
    public void load() throws BAOSReadException {
        this.get(Command.ON_OFF_STATUS).flatMap(DataPoint::getBoolean).ifPresent(currentStatus::set);
        this.get(Command.BRIGHTNESS_STATUS_ABS).flatMap(DataPoint::getUInt8).ifPresent(currentBrightness::set);
    }

}
