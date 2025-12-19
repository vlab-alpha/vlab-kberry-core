package tools.vlab.smarthome.kberry;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicFloat extends Number {

    private final AtomicInteger bits;

    public AtomicFloat() {
        this(0f);
    }

    public AtomicFloat(float initialValue) {
        this.bits = new AtomicInteger(Float.floatToIntBits(initialValue));
    }

    /**
     * Gibt den aktuellen Wert zurück.
     */
    public final float get() {
        return Float.intBitsToFloat(bits.get());
    }

    /**
     * Setzt den Wert atomar.
     */
    public final void set(float newValue) {
        bits.set(Float.floatToIntBits(newValue));
    }

    /**
     * Vergleicht den aktuellen Wert mit expectedValue und setzt ihn auf newValue,
     * wenn sie gleich sind. Dies geschieht atomar (CAS-Operation).
     */
    public final boolean compareAndSet(float expect, float update) {
        int expectBits = Float.floatToIntBits(expect);
        int updateBits = Float.floatToIntBits(update);
        return bits.compareAndSet(expectBits, updateBits);
    }

    /**
     * Addiert den angegebenen Delta-Wert zum aktuellen Wert und gibt den neuen Wert zurück.
     * Dies geschieht atomar.
     */
    public float addAndGet(float delta) {
        float currentVal;
        float newVal;
        do {
            currentVal = get();
            newVal = currentVal + delta;
            // Versucht atomar zu aktualisieren. Wenn ein anderer Thread dazwischenkommt,
            // wird die Schleife wiederholt (Compare-And-Swap).
        } while (!compareAndSet(currentVal, newVal));
        return newVal;
    }

    @Override
    public int intValue() {
        return (int) get();
    }

    @Override
    public long longValue() {
        return (long) get();
    }

    @Override
    public float floatValue() {
        return get();
    }

    @Override
    public double doubleValue() {
        return (double) get();
    }

    @Override
    public String toString() {
        return Float.toString(get());
    }

}
