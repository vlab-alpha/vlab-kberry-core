package tools.vlab.kberry.core.mqtt.shelly.devices;

import java.util.ArrayList;
import java.util.List;

public class IndexedStore<T> {

    private final List<T> list = new ArrayList<>();

    public int add(T value) {

        list.add(value);

        return list.size() - 1;

    }

    public T get(int index) {

        return list.get(index);

    }
}
