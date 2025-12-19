package tools.vlab.smarthome.kberry.devices.actor;

import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.KNXDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Dimmer extends KNXDevice {

    private final List<DimmerStatus> listener = new ArrayList<>();
    private final AtomicInteger currentBrightness = new AtomicInteger(0);
    private final AtomicBoolean currentStatus = new AtomicBoolean(false);

    private Dimmer(PositionPath positionPath) {
        super(positionPath,
                Command.ON_OFF_SWITCH,
                Command.SET_BRIGHTNESS, // Angenommen, Sie haben diesen Befehl hinzugefügt (oder nutzen DIMMEN direkt)
                Command.ON_OFF_STATUS, // Für Rückmeldungen,
                Command.BRIGHTNESS_STATUS
        );
    }

    public static Dimmer at(PositionPath positionPath) {
        return new Dimmer(positionPath);
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
