package tools.vlab.smarthome.kberry.devices.sensor;

import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.KNXDevice;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PresenceSensor extends KNXDevice {

    private final Vector<PresenceStatus> listener = new Vector<>();
    private final AtomicLong lastTrueMS = new AtomicLong(0);
    private final AtomicLong lastFalseMS = new AtomicLong(0);
    private final AtomicBoolean currentValue = new AtomicBoolean(false);

    private PresenceSensor(PositionPath positionPath) {
        super(positionPath, Command.PRESENCE_STATUS);
    }

    public static PresenceSensor at(PositionPath positionPath) {
        return new PresenceSensor(positionPath);
    }

    public void addListener(PresenceStatus listener) {
        this.listener.add(listener);
    }

    public long getLastPresentSecond() {
        return this.lastTrueMS.get() != 0 ? ((System.currentTimeMillis() - this.lastTrueMS.get()) / 1000) : -1;
    }

    public long getLastNoPresentSecond() {
        return this.lastFalseMS.get() != 0 ? ((System.currentTimeMillis() - this.lastFalseMS.get()) / 1000) : -1;
    }

    public boolean isPresent() {
        return currentValue.get();
    }

    @Override
    public void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case PRESENCE_STATUS -> dataPoint.getBoolean().ifPresent(newValue -> {
                long currentTime = System.currentTimeMillis();
                boolean oldOrCurrentValue = this.currentValue.getAndSet(newValue);
                if (oldOrCurrentValue != newValue) {
                    if (newValue) {
                        lastTrueMS.set(currentTime);
                    } else {
                        lastFalseMS.set(currentTime);
                    }
                    listener.forEach(presenceStatus -> presenceStatus.presenceChanged(this, newValue));
                }
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
//        this.get(Command.PRESENCE_STATUS).getBoolean().ifPresent(currentValue::set);
    }
}
