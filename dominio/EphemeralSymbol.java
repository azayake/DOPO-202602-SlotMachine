package dominio;

/**
 * A symbol that shrinks every time a wheel turn lands on it, until it
 * is only a point.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class EphemeralSymbol extends Symbol {

    /** Name of this kind of symbol. */
    public static final String KIND = "ephemeral";

    // How much of its size it loses each time it is selected.
    private static final int SHRINK_STEP = 20;

    // Current size, as a percentage of the normal one (0 = a point).
    private int scale;

    /**
     * @param color CSS color name of the symbol.
     */
    public EphemeralSymbol(String color) {
        super(color);
        scale = 100;
    }

    /**
     * Makes the symbol smaller, never below a point.
     */
    @Override
    public void selected() {
        scale = Math.max(0, scale - SHRINK_STEP);
    }

    /**
     * @return the current size as a percentage of the normal one.
     */
    @Override
    public int scalePercent() {
        return scale;
    }

    /**
     * @return the name of this kind of symbol.
     */
    @Override
    public String kind() {
        return KIND;
    }
}
