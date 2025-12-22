package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.Vector;

import static tools.vlab.kberry.core.devices.Command.ELECTRICITY_KWH_ACTUAL;
import static tools.vlab.kberry.core.devices.Command.ELECTRICITY_KWH_METER;

public class ElectricitySensor extends KNXDevice {

    private final Vector<ElectricStatus> listener = new Vector<>();
    private final PersistentValue<Integer> kwh;
    private final PersistentValue<Integer> kwhMeter;

    private ElectricitySensor(PositionPath positionPath,Integer refreshData) {
        super(positionPath, refreshData, ELECTRICITY_KWH_ACTUAL, ELECTRICITY_KWH_METER);
        this.kwh = new PersistentValue<>(positionPath, "kwh", 0, Integer.class);
        this.kwhMeter = new PersistentValue<>(positionPath, "kwhMeter", 0, Integer.class);
    }

    public static ElectricitySensor at(PositionPath positionPath) {
        return new ElectricitySensor(positionPath, null);
    }

    public static ElectricitySensor at(PositionPath positionPath, int refreshIntervallMs) {
        return new ElectricitySensor(positionPath, refreshIntervallMs);
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
