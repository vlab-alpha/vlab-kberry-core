package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;
import tools.vlab.kberry.core.devices.RGB;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class Led extends KNXDevice {

    private final PersistentValue<Boolean> status;
    private final PersistentValue<RGB> color;

    private Led(PositionPath positionPath, Integer refreshData) {
        super(positionPath,
                refreshData,
                Command.RGB_COLOR_CONTROL,
                Command.RGB_COLOR_STATUS,
                Command.ON_OFF_SWITCH,
                Command.ON_OFF_STATUS
        );
        this.status = new PersistentValue<>(positionPath, "ledStatus", false, Boolean.class);
        this.color = new PersistentValue<>(positionPath, "jalousieCurrentPosition", new RGB(0, 0, 0), RGB.class);
    }

    public static Led at(PositionPath positionPath) {
        return new Led(positionPath, null);
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
        if (rgb.isBlack()) {
            off();
        } else {
            this.set(Command.RGB_COLOR_CONTROL, rgb);
        }
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case ON_OFF_SWITCH -> dataPoint.getBoolean().ifPresent(value -> {
                this.status.set(value);
                getListener().forEach(status -> status.isOnChanged(this, value));
            });
            case RGB_COLOR_STATUS -> dataPoint.getRGB().ifPresent(value -> {
                this.color.set(value);
                getListener().forEach(status -> status.colorChanged(this, value));
            });
        }
    }

    private List<LedStatus> getListener() {
        return this.listeners.stream().filter(l -> l instanceof LedStatus).map(l -> (LedStatus) l).collect(Collectors.toList());
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.RGB_COLOR_STATUS).flatMap(DataPoint::getRGB).ifPresent(color::set);
        this.get(Command.ON_OFF_SWITCH).flatMap(DataPoint::getBoolean).ifPresent(status::set);
    }
}
