package tools.vlab.kberry.core.knx.devices;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.knx.baos.BAOSReadException;
import tools.vlab.kberry.core.knx.baos.messages.os.DataPoint;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class PushButton extends KNXDevice {

    private final Vector<PushButtonStatus> listener = new Vector<>();
    private final AtomicBoolean enable = new AtomicBoolean(false);

    private PushButton(PositionPath positionPath,Integer refreshData) {
        super(positionPath, refreshData, KnxCommand.ENABLE, KnxCommand.ENABLE_STATUS);
    }

    public static PushButton at(PositionPath positionPath) {
        return new PushButton(positionPath, null);
    }


    public void enable() {
        this.set(KnxCommand.ENABLE, true);
    }

    public void disable() {
        this.set(KnxCommand.ENABLE, false);
    }

    public boolean isEnable() {
        return this.enable.get();
    }

    @Override
    protected void received(KnxCommand command, DataPoint dataPoint) {
        switch (command) {
            case ENABLE_STATUS -> dataPoint.getBoolean().ifPresent(value -> {
                this.enable.set(value);
                listener.forEach(statusChanged -> statusChanged.enableChanged(this.getPositionPath(), value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(KnxCommand.ENABLE_STATUS).flatMap(DataPoint::getBoolean).ifPresent(enable::set);
    }
}
