package tools.vlab.kberry.core.knx.devices.sensor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.knx.baos.BAOSReadException;
import tools.vlab.kberry.core.knx.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.knx.devices.KnxCommand;
import tools.vlab.kberry.core.knx.devices.KNXDevice;
import tools.vlab.kberry.core.PersistentValue;

import java.util.List;
import java.util.stream.Collectors;

public class TemperatureSensor extends KNXDevice {

    private final PersistentValue<Float> currentTemp;

    private TemperatureSensor(PositionPath positionPath, Integer refreshData) {
        super(positionPath, refreshData, KnxCommand.TEMPERATURE_ACTUAL);
        this.currentTemp = new PersistentValue<>(positionPath, "TemperaturSensor", 0.0f, Float.class);
    }

    public static TemperatureSensor at(PositionPath positionPath) {
        return new TemperatureSensor(positionPath, null);
    }

    public static TemperatureSensor at(PositionPath positionPath, int refreshIntervallMs) {
        return new TemperatureSensor(positionPath, refreshIntervallMs);
    }

    public float getCurrentTemp() {
        return currentTemp.get();
    }

    @Override
    public void received(KnxCommand command, DataPoint dataPoint) {
        switch (command) {
            case TEMPERATURE_ACTUAL -> dataPoint.getFloat9().ifPresent(value -> {
                this.currentTemp.set(value);
                getListener().forEach(lightStatus -> lightStatus.temperatureChanged(this, value));
            });
        }
    }

    private List<TemperatureStatus> getListener() {
        return this.listeners.stream().filter(l -> l instanceof TemperatureStatus).map(l -> (TemperatureStatus) l).collect(Collectors.toList());
    }

    @Override
    public void load() throws BAOSReadException {
        if (this.currentTemp.isOlderThan(1000 * 60)) {
            this.get(KnxCommand.TEMPERATURE_ACTUAL).flatMap(DataPoint::getFloat9).ifPresent(currentTemp::set);
        }
    }
}
