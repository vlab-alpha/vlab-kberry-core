package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.List;
import java.util.stream.Collectors;

public class OnOffDevice extends KNXDevice {

    private final PersistentValue<Long> lastTrueMS;
    private final PersistentValue<Boolean> currentValue;

    protected OnOffDevice(PositionPath positionPath, Integer refreshData, String type) {
        super(positionPath, refreshData, Command.ON_OFF_SWITCH, Command.ON_OFF_STATUS);
        this.currentValue = new PersistentValue<>(positionPath, type + "_status", false, Boolean.class);
        this.lastTrueMS = new PersistentValue<>(positionPath, type + "_presence", 0L, Long.class);
    }

    public void on() {
        if (!currentValue.get()) {
            this.set(Command.ON_OFF_SWITCH, true, true);
        }
    }

    public void off() {
        if (currentValue.get()) {
            this.set(Command.ON_OFF_SWITCH, false);
        }
    }

    public boolean isOn() {
        return currentValue.get();
    }

    public long getLastPresentSecond() {
        return this.lastTrueMS.get() != 0 ? ((System.currentTimeMillis() - this.lastTrueMS.get()) / 1000) : -1;
    }

    @Override
    public void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case ON_OFF_STATUS, ON_OFF_SWITCH -> dataPoint.getBoolean().ifPresent(value -> {
                boolean oldOrCurrentValue = this.currentValue.getAndSet(value);
                if (oldOrCurrentValue != value) {
                    if (value) {
                        lastTrueMS.set(System.currentTimeMillis());
                    }
                    getListener().forEach(lightStatus -> lightStatus.onOffStatusChanged(this, value));
                }
            });
        }
    }

    private List<OnOffStatus> getListener() {
        return this.listeners.stream().filter(l -> l instanceof OnOffStatus).map(l -> (OnOffStatus) l).collect(Collectors.toList());
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.ON_OFF_SWITCH).flatMap(DataPoint::getBoolean).ifPresent(this.currentValue::set);
    }


}
