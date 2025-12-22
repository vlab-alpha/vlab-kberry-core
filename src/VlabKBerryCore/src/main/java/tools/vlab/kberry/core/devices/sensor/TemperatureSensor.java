package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.Vector;

public class TemperatureSensor extends KNXDevice {

    private final Vector<TemperatureStatus> listener = new Vector<>();
    private final PersistentValue<Float> currentTemp;

    private TemperatureSensor(PositionPath positionPath,Integer refreshData) {
        super(positionPath, refreshData, Command.TEMPERATURE_ACTUAL);
        this.currentTemp = new PersistentValue<>(positionPath, "TemperaturSensor", 0.0f, Float.class);
    }

    public static TemperatureSensor at(PositionPath positionPath) {
        return new TemperatureSensor(positionPath, null);
    }

    public static TemperatureSensor at(PositionPath positionPath, int refreshIntervallMs) {
        return new TemperatureSensor(positionPath, refreshIntervallMs);
    }

    public void addListener(TemperatureStatus listener) {
        this.listener.add(listener);
    }

    public float getCurrentTemp() {
        return currentTemp.get();
    }

    @Override
    public void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case TEMPERATURE_ACTUAL -> dataPoint.getFloat9().ifPresent(value -> {
                this.currentTemp.set(value);
                listener.forEach(lightStatus -> lightStatus.temperatureChanged(this, value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.TEMPERATURE_ACTUAL).flatMap(DataPoint::getFloat9).ifPresent(currentTemp::set);
    }
}
