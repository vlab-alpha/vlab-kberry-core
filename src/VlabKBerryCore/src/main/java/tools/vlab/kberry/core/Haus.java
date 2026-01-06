package tools.vlab.kberry.core;

public enum Haus implements PositionPath {

    // Ground Floor
    KitchenTop("Haus", "EG", "Küche", "Decke"),
    KitchenWall("Haus", "EG", "Küche", "Wand"),
    KitchenFloor("Haus", "EG", "Küche", "Boden"),
    LivingRoomTop("Haus", "EG", "Wohnzimmer", "Decke"),
    LivingRoomTV("Haus", "EG", "Wohnzimmer", "TV"),
    LivingRoomPlugin("Haus", "EG", "Wohnzimmer", "Steckdosen"),
    LivingRoomFloor("Haus", "EG", "Wohnzimmer", "Boden"),
    LivingRoomWall("Haus", "EG", "Wohnzimmer", "Wand"),
    DiningRoomTop("Haus", "EG", "Esszimmer", "Decke"),
    DiningRoomWall("Haus", "EG", "Esszimmer", "Wand"),
    DiningRoomFloor("Haus", "EG", "Esszimmer", "Boden"),
    HallwayTop("Haus", "EG", "Gang", "Decke"),
    HallwayWall("Haus", "EG", "Gang", "Wand"),
    HallwayFloor("Haus", "EG", "Gang", "Boden"),
    GuestWC_Top("Haus", "EG", "Gäste WC", "Decke"),
    GuestWC_Wall("Haus", "EG", "Gäste WC", "Wand"),
    ChangingRoomTop("Haus", "EG", "Umkleideraum", "Decke"),
    ChangingRoomWall("Haus", "EG", "Umkleideraum", "Wand"),
    ChangingRoomFloor("Haus", "EG", "Umkleideraum", "Boden"),

    // Upper Floor
    SleepingRoomTop("Haus", "OG", "Schlafzimmer", "Decke"),
    SleepingRoomWall("Haus", "OG", "Schlafzimmer", "Wand"),
    SleepingRoomFloor("Haus", "OG", "Schlafzimmer", "Floor"),
    KidsRoomYellowTop("Haus", "OG", "Gelb Kinderzimmer", "Decke"),
    KidsRoomYellowWall("Haus", "OG", "Gelb Kinderzimmer", "Wand"),
    KidsRoomYellowFloor("Haus", "OG", "Gelb Kinderzimmer", "Boden"),
    KidsRoomYellowPC("Haus", "OG", "Gelb Kinderzimmer", "PC"),
    KidsRoomBlueTop("Haus", "OG", "Blau Kinderzimmer", "Decke"),
    KidsRoomBlueWall("Haus", "OG", "Blau Kinderzimmer", "Wand"),
    KidsRoomBlueFloor("Haus", "OG", "Blau Kinderzimmer", "Boden"),
    OfficeTop("Haus", "OG", "Büro", "Decke"),
    OfficeWall("Haus", "OG", "Büro", "Wand"),
    OfficeFloor("Haus", "OG", "Büro", "Boden"),
    BathTop("Haus", "OG", "Bad", "Decke"),
    BathWall("Haus", "OG", "Bad", "Wand"),
    BathFloor("Haus", "OG", "Bad", "Boden"),
    UpperHallwayTop("Haus", "OG", "Gang", "Decke"),
    UpperHallwayWall("Haus", "OG", "Gang", "Wand"),
    UpperHallwayFloor("Haus", "OG", "Gang", "Boden");
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

    public static Haus positionPath(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        String[] parts = message.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Message must have 4 parts: Location.Floor.Room.Position");
        }
        String location = parts[0];
        String floor = parts[1];
        String room = parts[2];
        String position = parts[3];

        for (Haus h : values()) {
            if (h.getLocation().equalsIgnoreCase(location)
                    && h.getFloor().equalsIgnoreCase(floor)
                    && h.getRoom().equalsIgnoreCase(room)
                    && h.getPosition().equalsIgnoreCase(position)) {
                return h;
            }
        }

        throw new IllegalArgumentException("No matching Haus enum for path: " + message);
    }
}
