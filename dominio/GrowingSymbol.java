package dominio;

/**
 * Custom symbol of Cycle 4. It is the opposite of the ephemeral one:
 * every time a wheel turn lands on it, it grows, until it is 40%
 * bigger than normal (so it still fits in its place on the wheel).
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class GrowingSymbol extends Symbol {

    /** Name of this kind of symbol. */
    public static final String KIND = "growing";

    // How much size it gains each time it is selected.
    private static final int GROW_STEP = 20;

    // Biggest size it can reach, as a percentage of the normal one.
    private static final int MAX_SCALE = 140;

    // Current size, as a percentage of the normal one.
    private int scale;

    /**
     * @param color CSS color name of the symbol.
     */
    public GrowingSymbol(String color) {
        super(color);
        scale = 100;
    }

    /**
     * Makes the symbol bigger, up to the maximum.
     */
    @Override
    public void selected() {
        scale = Math.min(MAX_SCALE, scale + GROW_STEP);
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
