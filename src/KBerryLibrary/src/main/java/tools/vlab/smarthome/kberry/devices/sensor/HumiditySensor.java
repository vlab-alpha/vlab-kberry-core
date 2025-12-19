package tools.vlab.smarthome.kberry.devices.sensor;

import tools.vlab.smarthome.kberry.AtomicFloat;
import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.KNXDevice;

import java.util.Vector;

import static tools.vlab.smarthome.kberry.devices.Command.HUMIDITY_ACTUAL;

public class HumiditySensor extends KNXDevice {

    private final Vector<HumidityStatus> listener = new Vector<>();
    private final AtomicFloat currentHumidity = new AtomicFloat(0.0f);

    private HumiditySensor(PositionPath positionPath) {
        super(positionPath, HUMIDITY_ACTUAL);
    }

    public static HumiditySensor at(PositionPath positionPath) {
        return new HumiditySensor(positionPath);
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
