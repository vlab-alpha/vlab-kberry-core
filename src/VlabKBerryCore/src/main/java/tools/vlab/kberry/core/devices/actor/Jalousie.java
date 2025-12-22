package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.Vector;

// FIXME: manchmal reagieren die Jalousien nicht, es soll noch ein timer prüfen, ob es sich geändert hat (wenn keien Exception geschmissen worden ist) und dann soll nochmal getriggert werden!!!
public class Jalousie extends KNXDevice {

    private final Vector<JalousieStatus> listener = new Vector<>();
    private final PersistentValue<Integer> currentPosition;

    private Jalousie(PositionPath positionPath,Integer refreshData) {
        super(positionPath,
                refreshData,
                Command.SHUTTER_UP_DOWN_CONTROL,
                Command.STOP,
                Command.SHUTTER_REFERENCE,
                Command.SHUTTER_POSITION_SET,
                Command.SHUTTER_POSITION_ACTUAL_STATUS);
        this.currentPosition = new PersistentValue<>(positionPath, "jalousieCurrentPosition", 0, Integer.class);
    }

    public static Jalousie at(PositionPath positionPath) {
        return new Jalousie(positionPath, null);
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
