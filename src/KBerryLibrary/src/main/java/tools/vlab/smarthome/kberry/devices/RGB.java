package tools.vlab.smarthome.kberry.devices;

public record RGB(int r, int g, int b) {

    public int red() {
        return r;
    }
    public int green() {
        return g;
    }
    public int blue() {
        return b;
    }
}
