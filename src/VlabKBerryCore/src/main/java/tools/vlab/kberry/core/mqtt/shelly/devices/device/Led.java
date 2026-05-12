package tools.vlab.kberry.core.mqtt.shelly.devices.device;

import tools.vlab.kberry.core.PersistentValue;
import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.RGB;
import tools.vlab.kberry.core.RGBW;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyCommand;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyDataPoint;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyDevice;

import java.util.List;
import java.util.stream.Collectors;

import static tools.vlab.kberry.core.mqtt.shelly.devices.ShellyCommand.*;

public class Led extends ShellyDevice {

    private final PersistentValue<Boolean> status;
    private final PersistentValue<RGBW> color;
    private final PersistentValue<Integer> brightness;

    public Led(PositionPath positionPath, Integer refreshIntervalMs) {
        super(positionPath, refreshIntervalMs, "rgbw:0");
        this.status     = new PersistentValue<>(positionPath, "ledStatus",     false,                       Boolean.class);
        this.color      = new PersistentValue<>(positionPath, "ledColor",      new RGBW(255, 255, 255, 0),  RGBW.class);
        this.brightness = new PersistentValue<>(positionPath, "ledBrightness", 100,                         Integer.class);
    }

    // --- Status ---

    public boolean isOn() {
        return status.get();
    }

    public void on() {
        set(SET_RGBW_STATUS, ShellyDataPoint.rgbw(true, color.get(), brightness.get()));
    }

    public void off() {
        set(SET_RGBW_STATUS, ShellyDataPoint.rgbw(false, color.get(), brightness.get()));
    }

    // --- Farbe ---

    public RGBW getColor() {
        return color.get();
    }

    public void setColor(RGBW color) {
        set(SET_RGBW_STATUS, ShellyDataPoint.rgbw(status.get(), color, brightness.get()));
    }

    public void setColor(RGB rgb) {
        setColor(RGBW.from(rgb));
    }

    public void setColor(String hex) {
        setColor(RGBW.fromHex(hex));
    }

    public void setColor(String hex, int white) {
        setColor(RGBW.fromHex(hex, white));
    }

    // --- Helligkeit ---

    public int getBrightness() {
        return brightness.get();
    }

    public void setBrightness(int percent) {
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Brightness must be 0-100");
        set(SET_RGBW_STATUS, ShellyDataPoint.rgbw(status.get(), color.get(), percent));
    }

    // --- Kombi ---

    public void setColorAndBrightness(RGBW color, int brightness) {
        if (brightness < 0 || brightness > 100) throw new IllegalArgumentException("Brightness must be 0-100");
        set(SET_RGBW_STATUS, ShellyDataPoint.rgbw(status.get(), color, brightness));
    }

    // --- Load ---

    @Override
    public void load() {
        get(GET_RGBW_STATUS);
    }

    // --- Listener ---

    private List<LedStatus> getListener() {
        return this.listeners.stream()
                .filter(l -> l instanceof LedStatus)
                .map(l -> (LedStatus) l)
                .collect(Collectors.toList());
    }

    // --- Received ---

    @Override
    protected void received(ShellyCommand command, ShellyDataPoint datapoint) {
        switch (command) {
            case SET_RGBW_STATUS, GET_RGBW_STATUS, NOTIFY_STATUS -> {
                datapoint.getBoolean("output").ifPresent(v -> {
                    this.status.set(v);
                    getListener().forEach(l -> l.isOnChanged(this, v));
                });

                int r = datapoint.getInt("red").orElse(color.get().r());
                int g = datapoint.getInt("green").orElse(color.get().g());
                int b = datapoint.getInt("blue").orElse(color.get().b());
                int w = datapoint.getInt("white").orElse(color.get().w());
                RGBW newColor = new RGBW(r, g, b, w);

                if (!newColor.equals(color.get())) {
                    this.color.set(newColor);
                    getListener().forEach(l -> l.colorChanged(this, newColor));
                }

                datapoint.getInt("brightness").ifPresent(v -> {
                    this.brightness.set(v);
                    getListener().forEach(l -> l.brightnessChanged(this, v));
                });
            }
        }
    }
}