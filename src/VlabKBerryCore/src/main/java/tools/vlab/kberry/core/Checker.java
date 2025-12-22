package tools.vlab.kberry.core;

import tools.vlab.kberry.core.devices.KNXDevices;
import tools.vlab.kberry.core.devices.actor.Light;
import tools.vlab.kberry.core.devices.actor.OnOffDevice;
import tools.vlab.kberry.core.devices.actor.OnOffStatus;
import tools.vlab.kberry.core.devices.sensor.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class Checker implements PresenceStatus, OnOffStatus, VOCStatus, ElectricStatus,HumidityStatus {

    private final KNXDevices knxDevices;
    final AtomicBoolean switchOff = new AtomicBoolean(false);

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

                while (true) {
                    Thread.sleep(2000);
                    // schalteLichtAnAus(); // Funktioniert
                    this.getLuftfeuchtigkeit();
                    this.getVoc();

                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).run();
    }

    private void schalteLichtAnAus() {
        System.out.println("Schalte Büro Licht " + (switchOff.get() ? "an" : "aus"));
        var light = this.knxDevices.getKNXDevice(Light.class, HausTester.Office);

        light.ifPresent(l -> {
            System.out.println("Licht gefunden!");
            if (!switchOff.get()) {
                l.off();
            } else {
                l.on();
            }
            switchOff.set(!switchOff.get());
        });
    }

    private void getLuftfeuchtigkeit() {
        var humidity = this.knxDevices.getKNXDevice(HumiditySensor.class, HausTester.Kueche);
        humidity.ifPresent(h -> {
            System.out.println("Luftfeuchtigkeit " + h.getCurrentHumidity() + "%");
        });
    }

    private void getVoc() {
        var co2 = this.knxDevices.getKNXDevice(VOCSensor.class, HausTester.Kueche);
        co2.ifPresent(co -> {
            System.out.println("Co2: " + co.getCurrentPPM() + "ppm");
        });
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
