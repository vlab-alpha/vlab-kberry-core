package tools.vlab.kberry.core.devices;

public enum LuxCategory {
    // Enum-Konstanten mit empfohlenen Mindest-Lux-Werten
    DARKNESS(0, "Komplette Dunkelheit"),
    NIGHT_LIGHT(1, "Nachtlicht/Orientierungslicht (Flur nachts)"),
    VERY_LOW(10, "Sehr geringe Beleuchtung (Keller, Abstellraum)"),
    LOW(50, "Geringe Beleuchtung (Treppenhaus, Garage)"),
    AMBIENT(100, "Grundbeleuchtung (Wohnzimmer abends)"),
    NORMAL_WORK(300, "Normale Arbeitsplatzbeleuchtung (Büro, Küche)"),
    DETAIL_WORK(500, "Detailarbeiten (Lesen, Werkstatt)"),
    BRIGHT(1000, "Helle Umgebung (OP-Saal, sehr heller Tag)"),
    DIRECT_SUNLIGHT(10000, "Direktes Sonnenlicht (im Freien, sehr extrem)");

    private final int minLuxValue;
    private final String description;

    LuxCategory(int minLuxValue, String description) {
        this.minLuxValue = minLuxValue;
        this.description = description;
    }

    public int getMinLuxValue() {
        return minLuxValue;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Konvertiert einen tatsächlichen Lux-Wert (z.B. vom Sensor) in die entsprechende Kategorie.
     * Es wird die Kategorie zurückgegeben, deren Mindestwert am nächsten und kleiner/gleich dem Input-Wert ist.
     *
     * @param actualLuxValue Der gemessene Lux-Wert (float oder int).
     * @return Die entsprechende LuxCategory.
     */
    public static LuxCategory fromLuxValue(float actualLuxValue) {
        LuxCategory bestMatch = DARKNESS; // Startet mit der niedrigsten Kategorie

        // Durchläuft alle Kategorien, um die höchste passende zu finden
        for (LuxCategory category : LuxCategory.values()) {
            if (actualLuxValue >= category.minLuxValue) {
                if (category.minLuxValue > bestMatch.minLuxValue) {
                    bestMatch = category;
                }
            }
        }

        return bestMatch;
    }

}
