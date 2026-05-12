package tools.vlab.kberry.core.knx.devices.sensor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.knx.baos.BAOSReadException;
import tools.vlab.kberry.core.knx.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.knx.devices.KnxCommand;
import tools.vlab.kberry.core.knx.devices.KNXDevice;
import tools.vlab.kberry.core.PersistentValue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class PresenceSensor extends KNXDevice {

    private final PersistentValue<Long> lastTrueMS;
    private final AtomicBoolean currentValue = new AtomicBoolean(false);

    private PresenceSensor(PositionPath positionPath, Integer refreshData) {
        super(positionPath, refreshData, KnxCommand.PRESENCE_STATUS);
        this.lastTrueMS = new PersistentValue<>(positionPath, "presence", 0L, Long.class);
    }

    public static PresenceSensor at(PositionPath positionPath) {
        return new PresenceSensor(positionPath, null);
    }

    public long getLastPresentSecond() {
        return this.lastTrueMS.get() != 0 ? ((System.currentTimeMillis() - this.lastTrueMS.get()) / 1000) : -1;
    }


    public boolean isPresent() {
        return currentValue.get();
    }

    @Override
    public void received(KnxCommand command, DataPoint dataPoint) {
        switch (command) {
            case PRESENCE_STATUS -> dataPoint.getBoolean().ifPresent(newValue -> {
                long currentTime = System.currentTimeMillis();
                boolean oldOrCurrentValue = this.currentValue.getAndSet(newValue);
                if (oldOrCurrentValue != newValue) {
                    if (newValue) {
                        lastTrueMS.set(currentTime);
                    }
                    this.getListener().forEach(presenceStatus -> presenceStatus.presenceChanged(this, newValue));
                }
            });
        }
    }

    private List<PresenceStatus> getListener() {
        return this.listeners.stream().filter(l -> l instanceof PresenceStatus).map(l -> (PresenceStatus) l).collect(Collectors.toList());
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(KnxCommand.PRESENCE_STATUS).flatMap(DataPoint::getBoolean).ifPresent(currentValue::set);
    }



}
