package tools.vlab.smarthome.kberry.devices.sensor;

import tools.vlab.smarthome.kberry.AtomicFloat;
import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.KNXDevice;
import tools.vlab.smarthome.kberry.devices.LuxCategory;

import java.util.Vector;

public class LuxSensor extends KNXDevice {

    private final Vector<LuxStatus> listener = new Vector<>();
    private final AtomicFloat currentLux = new AtomicFloat(0.0f);

    private LuxSensor(PositionPath positionPath) {
        super(positionPath, Command.LUX_VALUE_ACTUAL);
    }

    public static LuxSensor at(PositionPath positionPath) {
        return new LuxSensor(positionPath);
    }

    public void addListener(LuxStatus listener) {
        this.listener.add(listener);
    }

    public float getCurrentLux() {
        return currentLux.get();
    }

    public LuxCategory getLuxCategory() {
        return LuxCategory.fromLuxValue(this.currentLux.get());
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case LUX_VALUE_ACTUAL -> dataPoint.getFloat9().ifPresent(value -> {
                this.currentLux.set(value);
                listener.forEach(luxStatus -> luxStatus.luxChanged(this, value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.LUX_VALUE_ACTUAL).flatMap(DataPoint::getFloat9).ifPresent(currentLux::set);
    }
}
