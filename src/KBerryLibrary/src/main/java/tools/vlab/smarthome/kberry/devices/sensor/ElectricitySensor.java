package tools.vlab.smarthome.kberry.devices.sensor;

import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.KNXDevice;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static tools.vlab.smarthome.kberry.devices.Command.ELECTRICITY_KWH_ACTUAL;
import static tools.vlab.smarthome.kberry.devices.Command.ELECTRICITY_KWH_METER;

public class ElectricitySensor extends KNXDevice {

    private final Vector<ElectricStatus> listener = new Vector<>();
    private final AtomicInteger kwh = new AtomicInteger(0);
    private final AtomicInteger kwhMeter = new AtomicInteger(0);

    private ElectricitySensor(PositionPath positionPath) {
        super(positionPath, ELECTRICITY_KWH_ACTUAL, ELECTRICITY_KWH_METER);
    }

    public static ElectricitySensor at(PositionPath positionPath) {
        return new ElectricitySensor(positionPath);
    }

    public void addListener(ElectricStatus listener) {
        this.listener.add(listener);
    }

    public int getCurrentKWH() {
        return kwh.get();
    }

    public int getCurrentKWHMeter() {
        return kwhMeter.get();
    }

    @Override
    public void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case ELECTRICITY_KWH_ACTUAL -> dataPoint.getUInt8().ifPresent(value -> {
                kwh.set(value);
                listener.forEach(presenceStatus -> presenceStatus.kwhChanged(this, value));
            });
            case ELECTRICITY_KWH_METER -> dataPoint.getUInt8().ifPresent(value -> {
                kwhMeter.set(value);
                listener.forEach(presenceStatus -> presenceStatus.electricityChanged(this, value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(ELECTRICITY_KWH_ACTUAL).flatMap(DataPoint::getUInt8).ifPresent(kwh::set);
        this.get(ELECTRICITY_KWH_METER).flatMap(DataPoint::getUInt8).ifPresent(kwhMeter::set);
    }
}
