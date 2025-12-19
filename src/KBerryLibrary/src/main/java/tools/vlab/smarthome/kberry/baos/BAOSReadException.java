package tools.vlab.smarthome.kberry.baos;

public class BAOSReadException extends Exception {
    public BAOSReadException(String message, InterruptedException e) {
        super(message, e);
    }

    public BAOSReadException(String message) {
        super(message);
    }

    public BAOSReadException(String message, Exception e) {
        super(message, e);
    }
}
