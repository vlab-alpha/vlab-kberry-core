package tools.vlab.kberry.core;

public interface PositionPath {
    String getLocation();
    String getFloor();
    String getRoom();
    String getPosition();

    default String getId() {
        return String.join(".", getLocation(), getFloor(), getRoom(), getPosition());
    }

    default boolean isSame(PositionPath positionPath) {
        return this.getId().equalsIgnoreCase(positionPath.getId());
    }

    default boolean isSamePath(String path) {
        return this.getPath().equalsIgnoreCase(path);
    }
    default String getPath() {
        return String.join("/", getLocation(), getFloor(), getRoom(), getPosition());
    }

    default String toId(String type) {
        return String.join(".", getLocation(), getFloor(), getRoom(), getPosition(), type);
    }

    default boolean sameRoom(PositionPath path) {
        return this.getLocation().equalsIgnoreCase(path.getLocation()) &&  this.getFloor().equalsIgnoreCase(path.getFloor()) && this.getRoom().equalsIgnoreCase(path.getRoom());
    }

    default boolean sameFloor(PositionPath path) {
        return this.getLocation().equalsIgnoreCase(path.getLocation()) && this.getFloor().equalsIgnoreCase(path.getFloor());
    }


}
