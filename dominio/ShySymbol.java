package dominio;

/**
 * A shy symbol: each time it is selected on a wheel it switches
 * between visible and invisible.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class ShySymbol extends Symbol {

    /** Name of this kind of symbol. */
    public static final String KIND = "shy";

    // Whether the symbol can currently be seen.
    private boolean shown;

    /**
     * @param color CSS color name of the symbol.
     */
    public ShySymbol(String color) {
        super(color);
        shown = true;
    }

    /**
     * Switches the visibility of the symbol.
     */
    @Override
    public void selected() {
        shown = !shown;
    }

    /**
     * @return true if the symbol is currently visible.
     */
    @Override
    public boolean isShown() {
        return shown;
    }

    /**
     * @return the name of this kind of symbol.
     */
    @Override
    public String kind() {
        return KIND;
    }
}
