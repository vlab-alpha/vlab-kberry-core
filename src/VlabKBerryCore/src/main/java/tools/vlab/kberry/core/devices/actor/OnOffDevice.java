package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.Vector;

public class OnOffDevice extends KNXDevice {

    private final Vector<OnOffStatus> listener = new Vector<>();
    private final PersistentValue<Boolean> currentValue;

    protected OnOffDevice(PositionPath positionPath, Integer refreshData, String type) {
        super(positionPath, refreshData, Command.ON_OFF_SWITCH, Command.ON_OFF_STATUS);
        this.currentValue = new PersistentValue<>(positionPath, type + "status", false, Boolean.class);
    }

    public void on() {
        this.set(Command.ON_OFF_SWITCH, true);
    }

    public void off() {
        this.set(Command.ON_OFF_SWITCH, false);
    }

    public boolean isOn() {
        return currentValue.get();
    }

    @Override
    public void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case ON_OFF_STATUS, ON_OFF_SWITCH -> dataPoint.getBoolean().ifPresent(value -> {
                boolean oldOrCurrentValue = this.currentValue.getAndSet(value);
                if (oldOrCurrentValue != value) {
                    listener.forEach(lightStatus -> lightStatus.onOffStatusChanged(this, value));
                }
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.ON_OFF_SWITCH).flatMap(DataPoint::getBoolean).ifPresent(this.currentValue::set);
    }

    public void addListener(OnOffStatus lightStatus) {
        this.listener.add(lightStatus);
    }

}
