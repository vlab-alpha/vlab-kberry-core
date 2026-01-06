package tools.vlab.kberry.core.devices;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class PushButton extends KNXDevice {

    private final Vector<PushButtonStatus> listener = new Vector<>();
    private final AtomicBoolean enable = new AtomicBoolean(false);

    private PushButton(PositionPath positionPath,Integer refreshData) {
        super(positionPath, refreshData, Command.ENABLE, Command.ENABLE_STATUS);
    }

    public static PushButton at(PositionPath positionPath) {
        return new PushButton(positionPath, null);
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

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case ENABLE_STATUS -> dataPoint.getBoolean().ifPresent(value -> {
                this.enable.set(value);
                listener.forEach(statusChanged -> statusChanged.enableChanged(this.getPositionPath(), value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.ENABLE_STATUS).flatMap(DataPoint::getBoolean).ifPresent(enable::set);
    }
}
