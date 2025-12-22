package tools.vlab.kberry.core.devices;

import tools.vlab.kberry.core.PositionPath;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

public class PersistentValue<T> {
    private final Path filePath;
    private final AtomicReference<T> internalValue;
    private final Class<T> type;

    public PersistentValue(PositionPath positionPath, String name, T defaultValue, Class<T> type) {
        this.type = type;
        this.filePath = Paths.get("storage", positionPath.toId(name) + ".dat");
        this.internalValue = new AtomicReference<>(load(defaultValue));

        try { Files.createDirectories(filePath.getParent()); } catch (IOException ignored) {}
    }

    private T load(T defaultValue) {
        if (!Files.exists(filePath)) return defaultValue;
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath.toFile()))) {
            if (type == Long.class) return type.cast(dis.readLong());
            if (type == Integer.class) return type.cast(dis.readInt());
            if (type == Boolean.class) return type.cast(dis.readBoolean());
            if (type == Float.class) return type.cast(dis.readFloat());
            if (type == RGB.class) return type.cast(new RGB(dis.readInt(), dis.readInt(), dis.readInt()));
            return defaultValue;
        } catch (IOException e) {
            return defaultValue;
        }
    }

    private synchronized void persist(T value) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(filePath.toFile()))) {
            if (type == Long.class) dos.writeLong((Long) value);
            else if (type == Integer.class) dos.writeInt((Integer) value);
            else if (type == Boolean.class) dos.writeBoolean((Boolean) value);
            else if (type == Float.class) dos.writeFloat((Float) value);
            else if (type == RGB.class) {
                RGB rgb = (RGB) value;
                dos.writeInt(rgb.r());
                dos.writeInt(rgb.g());
                dos.writeInt(rgb.b());
            }
            dos.flush();
        } catch (IOException e) {
            System.err.println("Fehler beim Persistieren von " + filePath + ": " + e.getMessage());
        }
    }

    public T get() {
        return internalValue.get();
    }

    public void set(T newValue) {
        internalValue.set(newValue);
        persist(newValue);
    }

    // Speziell für AtomicLong-ähnliches Verhalten (Increment)
    public synchronized long incrementAndGet() {
        if (type != Long.class && type != Integer.class) throw new UnsupportedOperationException("Nur für Long");
        long next = (Long) internalValue.get() + 1;
        set(type.cast(next));
        return next;
    }

    public synchronized T getAndSet(T newValue) {
        T oldValue = internalValue.get();
        internalValue.set(newValue);
        persist(newValue);
        return oldValue;
    }
}
