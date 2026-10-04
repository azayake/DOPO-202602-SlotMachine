package dominio;

/**
 * A symbol of the slot machine, identified by its color.
 *
 * Symbol is the root of the symbol hierarchy. Every kind of symbol
 * (NormalSymbol, EphemeralSymbol, ShySymbol, GrowingSymbol) only
 * overrides the three small hooks below, so neither the wheel nor the
 * machine ever has to ask "what type of symbol is this?".
 *
 * @author Jhazael and Santiago
 * @version 2.1 (Cycle 4 - 2026-2)
 */
public abstract class Symbol {

    // CSS color that identifies this symbol, for example "red".
    private String color;

    /**
     * @param color CSS color name, for example "red" or "blue".
     */
    public Symbol(String color) {
        this.color = color;
    }

    /**
     * @return the name of this kind of symbol ("normal", "shy", ...). The
     * screen uses it to draw each kind with its own look.
     */
    public abstract String kind();

    /**
     * @return the CSS color that identifies this symbol.
     */
    public String getColor() {
        return color;
    }

    /**
     * Called by a wheel every time this symbol becomes the one it is
     * showing (a wheel turn lands on it). A normal symbol does nothing.
     */
    public void selected() {
    }

    /**
     * @return the size of the symbol when it is drawn, as a percentage
     * of the normal size. A normal symbol is always 100.
     */
    public int scalePercent() {
        return 100;
    }

    /**
     * @return true if the symbol can be seen when a wheel shows it.
     * A normal symbol is always seen.
     */
    public boolean isShown() {
        return true;
    }
}
