package tools.vlab.smarthome.kberry;

import tools.vlab.smarthome.kberry.baos.ByteUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    private final static DateFormat dateFormat = new SimpleDateFormat("mm:ss.SSS");

    public static void debug(String msg, byte[] frame) {
        System.out.println(System.currentTimeMillis() + " " + msg + " :" + bytesToHex(frame));
    }

    private static String bytesToHex(byte[] data) {
        return ByteUtil.toHex(data);
    }

    public static void debug(String format, Object... objects) {
        System.out.printf(now() + "[DEB] " + format + "\n", objects);
    }

    public static void error(String message, Object... objects) {
        System.err.printf(now() + "[ERROR] " + message + "\n", objects);
    }

    public static void error(Exception e, String message, Object... objects) {
        e.printStackTrace();
        System.err.printf(now() + message + "\n", objects);
    }

    public static void info(String message, Object... objects) {
        System.out.printf(now() + "[INFO] " + message + "\n", objects);
    }

    public static void debug(Exception e, String message, Object... objects) {
        System.err.printf(now() + "[ERROR] " + message + "\n" + e.getMessage() + "\n", objects);
    }

    private static String now() {
        return String.format("%s - ", dateFormat.format(new Date()));
    }
}
