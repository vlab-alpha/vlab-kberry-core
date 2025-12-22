package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.ArrayList;
import java.util.List;

public class Dimmer extends KNXDevice {

    private final List<DimmerStatus> listener = new ArrayList<>();
    private final PersistentValue<Integer> currentBrightness;
    private final PersistentValue<Boolean> currentStatus;

    private Dimmer(PositionPath positionPath,Integer refreshData) {
        super(positionPath,
                refreshData,
                Command.ON_OFF_SWITCH,
                Command.SET_BRIGHTNESS, // Angenommen, Sie haben diesen Befehl hinzugefügt (oder nutzen DIMMEN direkt)
                Command.ON_OFF_STATUS, // Für Rückmeldungen,
                Command.BRIGHTNESS_STATUS
        );
        this.currentBrightness = new PersistentValue<>(positionPath, "currentBrightness", 0, Integer.class);
        this.currentStatus = new PersistentValue<>(positionPath, "currentStatus", false, Boolean.class);
    }

    public static Dimmer at(PositionPath positionPath) {
        return new Dimmer(positionPath, null);
    }

    public void addListener(DimmerStatus status) {
        this.listener.add(status);
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
        this.set(Command.SET_BRIGHTNESS, percent);
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case ON_OFF_STATUS -> dataPoint.getBoolean().ifPresent(value -> {
                currentStatus.set(value);
                listener.forEach(status -> status.isOnChanged(this, value));

            });
            case BRIGHTNESS_STATUS -> dataPoint.getUInt8().ifPresent(value -> {
                currentBrightness.set(value);
                listener.forEach(status -> status.brightnessChanged(this, value));
            });
        }
    }


    @Override
    public void load() throws BAOSReadException {
        this.get(Command.ON_OFF_STATUS).flatMap(DataPoint::getBoolean).ifPresent(currentStatus::set);
        this.get(Command.BRIGHTNESS_STATUS).flatMap(DataPoint::getUInt8).ifPresent(currentBrightness::set);
    }

}
