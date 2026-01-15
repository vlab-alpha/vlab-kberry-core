package tools.vlab.kberry.core.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.kberry.core.PositionPath;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

public class PersistentValue<T> {

    private final static Logger Log = LoggerFactory.getLogger(PersistentValue.class);

    private final Path filePath;
    private final AtomicReference<T> internalValue;
    private final AtomicReference<Long> timestamp;
    private final Class<T> type;

    public PersistentValue(PositionPath positionPath, String name, T defaultValue, Class<T> type) {
        this.type = type;
        this.filePath = Paths.get("storage", positionPath.toId(name) + ".dat");
        LoadedValue<T> loaded = load(defaultValue);
        this.internalValue = new AtomicReference<>(loaded.value());
        this.timestamp = new AtomicReference<>(loaded.timestamp());

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException ignored) {
        }
    }

    private LoadedValue<T> load(T defaultValue) {
        if (!Files.exists(filePath)) {
            return new LoadedValue<>(defaultValue, System.currentTimeMillis());
        }
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath.toFile()))) {
            long ts = dis.readLong();
            if (type == Long.class) return new LoadedValue<>(type.cast(dis.readLong()), ts);
            if (type == Integer.class) return new LoadedValue<>(type.cast(dis.readInt()), ts);
            if (type == Boolean.class) return new LoadedValue<>(type.cast(dis.readBoolean()), ts);
            if (type == Float.class) return new LoadedValue<>(type.cast(dis.readFloat()), ts);
            if (type == RGB.class) {
                return new LoadedValue<>(
                        type.cast(new RGB(dis.readInt(), dis.readInt(), dis.readInt())),
                        ts
                );
            }

            return new LoadedValue<>(defaultValue, ts);
        } catch (IOException e) {
            return new LoadedValue<>(defaultValue, System.currentTimeMillis());
        }
    }

    private synchronized void persist(T value) {
        long now = System.currentTimeMillis();
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(filePath.toFile()))) {
            dos.writeLong(now);
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
            timestamp.set(now);
            Log.debug("Persisted value: {} to path {}", value, this.filePath.getFileName());
        } catch (IOException e) {
            Log.error("Could not save data to {}", filePath, e);
        }
    }

    public T get() {
        return internalValue.get();
    }

    public long getTimestamp() {
        return timestamp.get();
    }

    public void set(T newValue) {
        internalValue.set(newValue);
        persist(newValue);
    }

    public long getAgeMillis() {
        return System.currentTimeMillis() - timestamp.get();
    }

    public boolean isOlderThan(long millis) {
        return getAgeMillis() > millis;
    }

    public synchronized long incrementAndGet() {
        if (type != Long.class && type != Integer.class) throw new UnsupportedOperationException("Only For Long!");
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

    private record LoadedValue<T>(T value, long timestamp) {}
}
