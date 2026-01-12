package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.LuxCategory;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.List;
import java.util.stream.Collectors;

public class LuxSensor extends KNXDevice {

    private final PersistentValue<Float> smoothedLux;
    private static final float SMOOTHING_FACTOR = 0.1f;
    private static final float DEAD_ZONE = 15f;
    private final PersistentValue<Float> currentLux;

    private LuxSensor(PositionPath positionPath, Integer refreshData) {
        super(positionPath, refreshData, Command.LUX_VALUE_ACTUAL);
        this.currentLux = new PersistentValue<>(positionPath, "Lux", 0.0f, Float.class);
        this.smoothedLux = new PersistentValue<>(
                positionPath,
                "LuxSmoothed",
                0.0f,
                Float.class
        );
    }

    public static LuxSensor at(PositionPath positionPath) {
        return new LuxSensor(positionPath, null);
    }

    public static LuxSensor at(PositionPath positionPath, int refreshIntervallMs) {
        return new LuxSensor(positionPath, refreshIntervallMs);
    }

    public float getCurrentLux() {
        return currentLux.get();
    }

    public float getSmoothedLux() {
        return smoothedLux.get();
    }

    public LuxCategory getLuxCategory() {
        return LuxCategory.fromLuxValue(this.smoothedLux.get());
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case LUX_VALUE_ACTUAL -> dataPoint.getFloat9().ifPresent(raw -> {
                this.currentLux.set(raw);
                float filtered = smooth(raw);
                smoothedLux.set(filtered);
                getListener().forEach(luxStatus -> luxStatus.luxChanged(this, filtered));
            });
        }
    }

    private List<LuxStatus> getListener() {
        return this.listeners.stream().filter(l -> l instanceof LuxStatus).map(l -> (LuxStatus) l).collect(Collectors.toList());
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.LUX_VALUE_ACTUAL).flatMap(DataPoint::getFloat9).ifPresent(lux -> {
            currentLux.set(lux);
            smoothedLux.set(lux);
        });
    }

    private float smooth(float rawLux) {
        float previous = smoothedLux.get();
        if (previous == 0.0f) {
            return rawLux;
        }

        if (Math.abs(rawLux - previous) < DEAD_ZONE) {
            return previous;
        }

        return previous + SMOOTHING_FACTOR * (rawLux - previous);
    }
}
