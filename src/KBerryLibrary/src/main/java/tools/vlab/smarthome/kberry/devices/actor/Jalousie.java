package tools.vlab.smarthome.kberry.devices.actor;

import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.KNXDevice;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

// FIXME: manchmal reagieren die Jalousien nicht, es soll noch ein timer prüfen, ob es sich geändert hat (wenn keien Exception geschmissen worden ist) und dann soll nochmal getriggert werden!!!
public class Jalousie extends KNXDevice {

    private final Vector<JalousieStatus> listener = new Vector<>();
    private final AtomicInteger currentPosition = new AtomicInteger(0);

    private Jalousie(PositionPath positionPath) {
        super(positionPath,
                Command.SHUTTER_UP_DOWN_CONTROL,
                Command.STOP,
                Command.SHUTTER_REFERENCE,
                Command.SHUTTER_POSITION_SET,
                Command.SHUTTER_POSITION_ACTUAL_STATUS);
    }

    public static Jalousie at(PositionPath positionPath) {
        return new Jalousie(positionPath);
    }

    public void addListener(JalousieStatus position) {
        this.listener.add(position);
    }

    public void up() {
        this.set(Command.SHUTTER_UP_DOWN_CONTROL, false);
    }

    public void down() {
        this.set(Command.SHUTTER_UP_DOWN_CONTROL, true);
    }

    public void referenceDriving() {
        this.set(Command.SHUTTER_REFERENCE, true);
    }

    public void stop() {
        this.set(Command.STOP, true);
    }

    public int currentPosition() {
        return this.currentPosition.get();
    }

    public void setPosition(int position) {
        this.set(Command.SHUTTER_POSITION_SET, position);
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case SHUTTER_POSITION_ACTUAL_STATUS -> dataPoint.getUInt8().ifPresent(value -> {
                this.currentPosition.set(value);
                listener.forEach(status-> status.positionChanged(this, value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.SHUTTER_POSITION_ACTUAL_STATUS).flatMap(DataPoint::getUInt8).ifPresent(currentPosition::set);
    }
}
