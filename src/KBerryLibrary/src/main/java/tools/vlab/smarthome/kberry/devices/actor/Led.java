package tools.vlab.smarthome.kberry.devices.actor;

import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.KNXDevice;
import tools.vlab.smarthome.kberry.devices.RGB;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Led extends KNXDevice {

    private final Vector<LedStatus> listener = new Vector<>();
    private final AtomicBoolean status = new AtomicBoolean(false);
    private final AtomicReference<RGB> color = new AtomicReference<>(new RGB(0, 0, 0));

    private Led(PositionPath positionPath) {
        super(positionPath,
                Command.RGB_COLOR_CONTROL,
                Command.RGB_COLOR_STATUS,
                Command.ON_OFF_SWITCH,
                Command.ON_OFF_STATUS
        );
    }

    public Led at(PositionPath positionPath) {
        return new Led(positionPath);
    }

    public void addListener(LedStatus ledStatus) {
        this.listener.add(ledStatus);
    }

    public boolean isOn() {
        return status.get();
    }

    public void on() {
        this.set(Command.ON_OFF_SWITCH, true);
    }

    public void off() {
        this.set(Command.ON_OFF_SWITCH, false);
    }

    public RGB getRGB() {
        return this.color.get();
    }

    public void setRGB(RGB rgb) {
        this.set(Command.RGB_COLOR_CONTROL, rgb);
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case ON_OFF_SWITCH -> dataPoint.getBoolean().ifPresent(value -> {
                this.status.set(value);
                listener.forEach(status -> status.isOnChanged(this, value));
            });
            case RGB_COLOR_STATUS -> dataPoint.getRGB().ifPresent(value -> {
                this.color.set(value);
                listener.forEach(status -> status.colorChanged(this, value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.RGB_COLOR_STATUS).flatMap(DataPoint::getRGB).ifPresent(color::set);
        this.get(Command.ON_OFF_SWITCH).flatMap(DataPoint::getBoolean).ifPresent(status::set);
    }
}
