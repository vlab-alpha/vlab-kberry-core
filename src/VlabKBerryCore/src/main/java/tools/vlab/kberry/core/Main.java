package tools.vlab.kberry.core;

import tools.vlab.kberry.core.baos.SerialBAOSConnection;
import tools.vlab.kberry.core.baos.TimeoutException;
import tools.vlab.kberry.core.devices.KNXDevices;
import tools.vlab.kberry.core.devices.PushButton;
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
            connection = new SerialBAOSConnection("/dev/ttyAMA0", 5000, 10);

            KNXDevices devices = new KNXDevices(connection);
// Push Taster
            devices.register(PushButton.at(Haus.KidsRoomYellowWall));
            devices.register(PushButton.at(Haus.KidsRoomBlueWall));
            devices.register(Light.at(Haus.BathTop));
            devices.register(Light.at(Haus.BathWall));
            devices.register(Light.at(Haus.KidsRoomBlueTop));
            devices.register(Light.at(Haus.KidsRoomYellowTop));
            devices.register(Light.at(Haus.OfficeTop));
            devices.register(Light.at(Haus.SleepingRoomTop));
            devices.register(Light.at(Haus.UpperHallwayTop));
            devices.register(Light.at(Haus.HallwayTop));
            devices.register(Light.at(Haus.GuestWC_Top));
            devices.register(Light.at(Haus.ChangingRoomTop));
            devices.register(Light.at(Haus.DiningRoomTop));
            devices.register(Light.at(Haus.LivingRoomTop));
            devices.register(Light.at(Haus.KitchenTop));


            devices.exportCSV(Path.of("weinzierl_export.csv"));

            checker = new Checker(devices);
            devices.getKNXDevices(Light.class).forEach(device -> device.addListener(checker));
            devices.getKNXDevices(PushButton.class).forEach(device -> device.addListener(checker));
            //devices.getKNXDevice(Light.class,Haus.GuestWC_Top).ifPresent(device -> device.addListener(checker));
            //devices.getKNXDevice(Light.class,Haus.SleepingRoomTop).ifPresent(device -> device.addListener(checker));
            System.out.println("Add Listener Check");

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
