package tools.vlab.kberry.core.devices;

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


    public String toHex() {
        validate();
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public static RGB fromHex(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("Hex string must not be null");
        }

        String value = hex.startsWith("#") ? hex.substring(1) : hex;

        if (value.length() < 6) {
            throw new IllegalArgumentException("Hex color must be 6 characters long");
        }

        try {
            int r = Integer.parseInt(value.substring(0, 2), 16);
            int g = Integer.parseInt(value.substring(2, 4), 16);
            int b = Integer.parseInt(value.substring(4, 6), 16);
            return new RGB(r, g, b);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex color: " + hex, e);
        }
    }

    private void validate() {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalStateException("RGB values must be between 0 and 255");
        }
    }

    public boolean isBlack() {
        return r == 0 && b == 0 && g == 0;
    }
}
