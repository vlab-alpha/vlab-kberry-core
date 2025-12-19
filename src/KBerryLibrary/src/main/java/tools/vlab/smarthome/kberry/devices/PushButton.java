package tools.vlab.smarthome.kberry.devices;

import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class PushButton extends KNXDevice {

    private final Vector<PushButtonStatus> listener = new Vector<>();
    private final AtomicBoolean statusChanged = new AtomicBoolean(false);
    private final AtomicBoolean enable = new AtomicBoolean(false);

    private PushButton(PositionPath positionPath) {
        super(positionPath, Command.ON_OFF_STATUS, Command.ENABLE, Command.ENABLE_STATUS);
    }

    public static PushButton at(PositionPath positionPath) {
        return new PushButton(positionPath);
    }

    public void addListener(PushButtonStatus listener) {
        this.listener.add(listener);
    }

    public void enable() {
        this.set(Command.ENABLE, true);
    }

    public void disable() {
        this.set(Command.ENABLE, false);
    }

    public boolean isEnable() {
        return this.enable.get();
    }

    public boolean isOn() {
        return this.statusChanged.get();
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case ENABLE_STATUS -> dataPoint.getBoolean().ifPresent(value -> {
                this.enable.set(value);
                listener.forEach(statusChanged -> statusChanged.enableChanged(this.getPositionPath(), value));
            });
            case ON_OFF_STATUS -> dataPoint.getBoolean().ifPresent(value -> {
                this.statusChanged.set(value);
                listener.forEach(statusChanged -> statusChanged.pushButtonStatusChanged(this.getPositionPath(), value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.ENABLE_STATUS).flatMap(DataPoint::getBoolean).ifPresent(enable::set);
        this.get(Command.ON_OFF_STATUS).flatMap(DataPoint::getBoolean).ifPresent(statusChanged::set);
    }
}
