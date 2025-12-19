package tools.vlab.smarthome.kberry;

public enum Haus implements PositionPath {
    KinderzimmerGelbDecke("Haus", "1OG", "Kinderzimmer Gelb", "Decke"),
    KinderzimmerGelbSteckdose("Haus", "1OG", "Kinderzimmer Gelb", "Steckdose"),
    KinderzimmerBlau("Haus", "1OG", "Kinderzimmer Blau", "Decke"),
    Office("Haus", "1OG", "Büro", "Decke"),
    Kueche("Haus", "EG", "Küche", "Decke"),
    ;

    private final String position;
    private final String floor;
    private final String room;
    private final String roomPosition;

    Haus(String position, String floor, String room, String roomPosition) {
        this.position = position;
        this.floor = floor;
        this.room = room;
        this.roomPosition = roomPosition;
    }

    @Override
    public String getLocation() {
        return this.position;
    }

    @Override
    public String getFloor() {
        return this.floor;
    }

    @Override
    public String getRoom() {
        return this.room;
    }

    @Override
    public String getPosition() {
        return this.roomPosition;
    }
}
