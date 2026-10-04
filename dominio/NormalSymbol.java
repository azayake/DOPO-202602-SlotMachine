package dominio;

/**
 * The standard symbol: it always looks the same.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class NormalSymbol extends Symbol {

    /** Name of this kind of symbol. */
    public static final String KIND = "normal";

    /**
     * @param color CSS color name of the symbol.
     */
    public NormalSymbol(String color) {
        super(color);
    }

    /**
     * @return the name of this kind of symbol.
     */
    @Override
    public String kind() {
        return KIND;
    }
}
