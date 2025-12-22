package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.Vector;

import static tools.vlab.kberry.core.devices.Command.HUMIDITY_ACTUAL;

public class HumiditySensor extends KNXDevice {

    private final Vector<HumidityStatus> listener = new Vector<>();
    private final PersistentValue<Float> currentHumidity;

    private HumiditySensor(PositionPath positionPath,Integer refreshData) {
        super(positionPath, refreshData, HUMIDITY_ACTUAL);
        this.currentHumidity = new PersistentValue<>(positionPath, "humidity", 0.0f, Float.class);
    }

    public static HumiditySensor at(PositionPath positionPath) {
        return new HumiditySensor(positionPath,null);
    }

    public static HumiditySensor at(PositionPath positionPath, int refreshIntervallMs) {
        return new HumiditySensor(positionPath,refreshIntervallMs);
    }

    public void addListener(HumidityStatus listener) {
        this.listener.add(listener);
    }

    public float getCurrentHumidity() {
        return currentHumidity.get();
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case HUMIDITY_ACTUAL -> dataPoint.getFloat9().ifPresent(value -> {
                this.currentHumidity.set(value);
                listener.forEach(humidityStatus -> humidityStatus.humidityChanged(this, value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(HUMIDITY_ACTUAL).flatMap(DataPoint::getFloat9).ifPresent(currentHumidity::set);
    }
}
