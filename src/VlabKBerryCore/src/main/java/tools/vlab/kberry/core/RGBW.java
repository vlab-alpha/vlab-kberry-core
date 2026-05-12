package tools.vlab.kberry.core;

public record RGBW(int r, int g, int b, int w) {

    public RGB toRGB() {
        return new RGB(r, g, b);
    }

    public int white() {
        return w;
    }

    public static RGBW from(RGB rgb) {
        return new RGBW(rgb.r(), rgb.g(), rgb.b(), 0);
    }

    public static RGBW from(RGB rgb, int white) {
        return new RGBW(rgb.r(), rgb.g(), rgb.b(), white);
    }

    public static RGBW fromHex(String hex) {
        RGB rgb = RGB.fromHex(hex);
        return new RGBW(rgb.r(), rgb.g(), rgb.b(), 0);
    }

    public static RGBW fromHex(String hex, int white) {
        RGB rgb = RGB.fromHex(hex);
        return new RGBW(rgb.r(), rgb.g(), rgb.b(), white);
    }

    public String toHex() {
        return toRGB().toHex();
    }

    public boolean isOff() {
        return r == 0 && g == 0 && b == 0 && w == 0;
    }
}