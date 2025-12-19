package tools.vlab.smarthome.kberry;

import tools.vlab.smarthome.kberry.devices.KNXDevices;
import tools.vlab.smarthome.kberry.devices.actor.OnOffDevice;
import tools.vlab.smarthome.kberry.devices.actor.OnOffStatus;
import tools.vlab.smarthome.kberry.devices.sensor.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class Checker implements PresenceStatus, OnOffStatus, VOCStatus, ElectricStatus,HumidityStatus {

    private final KNXDevices knxDevices;

    public Checker(KNXDevices knxDevices) {
        this.knxDevices = knxDevices;
    }

    public void start() {
        new Thread(this::run).start();
    }

    @Override
    public void presenceChanged(PresenceSensor sensor, boolean available) {
        System.out.println("Presence changed in " + sensor.getPositionPath().getPath() + " : " + available + " last: " + sensor.getLastPresentSecond() + "s");
    }

    private void run() {
        ((Runnable) () -> {
            try {
                final AtomicBoolean switchOff = new AtomicBoolean(false);
                while (true) {
                    Thread.sleep(2000);
//                    System.out.println("Schalte Büro Licht " + (switchOff.get() ? "an" : "aus"));
//                    var light = this.knxDevices.getKNXDevice(Light.class, Haus.Office);
//                    light.ifPresent(l -> {
//                        System.out.println("Licht gefunden!");
//                        if (switchOff.get()) {
//                            l.off();
//                        } else {
//                            l.on();
//                        }
//                        switchOff.set(!switchOff.get());
//                    });
                    Thread.sleep(5000);
                    this.knxDevices.getKNXDevices(VOCSensor.class).forEach(device -> {
                        System.out.println("VOC (REQ) " + device.getCurrentPPM() + "ppm");
                    });
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).run();
    }

    @Override
    public void onOffStatusChanged(OnOffDevice device, boolean isOn) {
        System.out.println("Light status " + device.getPositionPath().getPath() + " : " + isOn);
    }

    @Override
    public void vocChanged(VOCSensor sensor, float voc) {
        System.out.println("VOC (IND) " + sensor.getPositionPath().getPath() + " : " + voc + "ppm");
    }

    @Override
    public void kwhChanged(ElectricitySensor sensor, float kwh) {
        System.out.println("Strom " + sensor.getPositionPath().getPath() + " : " + kwh + "kwh");
    }

    @Override
    public void electricityChanged(ElectricitySensor sensor, int electricity) {
        System.out.println("Strom " + sensor.getPositionPath().getPath() + " : " + electricity + "Ah");
    }

    @Override
    public void humidityChanged(HumiditySensor sensor, float humidity) {
        System.out.println("Luftfeuchtigkeit " + sensor.getPositionPath().getPath() + " : " + humidity + "l");
    }
}
