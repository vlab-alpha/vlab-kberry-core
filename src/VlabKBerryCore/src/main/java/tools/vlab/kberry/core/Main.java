package tools.vlab.kberry.core;

import tools.vlab.kberry.core.baos.SerialBAOSConnection;
import tools.vlab.kberry.core.baos.TimeoutException;
import tools.vlab.kberry.core.devices.KNXDevices;
import tools.vlab.kberry.core.devices.actor.Light;
import tools.vlab.kberry.core.devices.sensor.HumiditySensor;
import tools.vlab.kberry.core.devices.sensor.PresenceSensor;
import tools.vlab.kberry.core.devices.sensor.VOCSensor;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    private static Checker checker;

    public static void main(String[] args) throws IOException {
        SerialBAOSConnection connection = null;
        try {
            connection = new SerialBAOSConnection("/dev/ttyAMA0", 1000, 10);

            KNXDevices devices = new KNXDevices(connection);

            devices.register(PresenceSensor.at(HausTester.Office));
//            devices.register(PresenceSensor.at(Haus.Office));
            devices.register(Light.at(HausTester.Office));
            devices.register(PresenceSensor.at(HausTester.KinderzimmerGelbDecke));
//            devices.register(Light.at(Haus.KinderzimmerBlau));
//            devices.register(Light.at(Haus.KinderzimmerGelbDecke));
            devices.register(VOCSensor.at(HausTester.Kueche));
            devices.register(HumiditySensor.at(HausTester.Kueche));
//            devices.register(ElectricitySensor.at(Haus.KinderzimmerGelbSteckdose));

            devices.exportCSV(Path.of("weinzierl_export.csv"));

            checker = new Checker(devices);

            System.out.println("Add Listener Check");
            devices.getKNXDevices(PresenceSensor.class).forEach(device -> device.addListener(checker));
            devices.getKNXDevices(Light.class).forEach(device -> device.addListener(checker));
            devices.getKNXDevices(VOCSensor.class).forEach(device -> device.addListener(checker));
//            devices.getKNXDevices(ElectricitySensor.class).forEach(device -> device.addListener(checker));
            devices.getKNXDevices(HumiditySensor.class).forEach(device -> device.addListener(checker));
            devices.getKNXDevice(Light.class, HausTester.Office).get().on();
            System.out.println("Starte Listener Check");
            connection.connect();
            checker.start();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
//            if (connection != null) {
//                try {
//                    connection.disconnect();
//                } catch (Exception e) {
//                    System.err.println("Error while disconnecting");
//                    e.printStackTrace();
//                }
//
//            }
        }

    }

}
