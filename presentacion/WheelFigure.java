package presentacion;

/**
 * What the screen needs to know to draw ONE wheel. It is plain data:
 * the domain builds one figure per wheel and hands them to the view,
 * so the view never sees the classes of the domain.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class WheelFigure {

    private final String color;
    private final int scalePercent;
    private final boolean symbolShown;
    private final String symbolKind;
    private final String wheelKind;
    private final boolean locked;

    /**
     * @param color        color name of the symbol being shown (null if the machine has no symbols).
     * @param scalePercent size of that symbol as a percentage of the normal one.
     * @param symbolShown  false if the symbol is hiding (shy symbol).
     * @param symbolKind   kind of the symbol (decides its shape).
     * @param wheelKind    kind of the wheel (decides its mark).
     * @param locked       whether the wheel is locked.
     */
    public WheelFigure(String color, int scalePercent, boolean symbolShown,
                       String symbolKind, String wheelKind, boolean locked) {
        this.color = color;
        this.scalePercent = scalePercent;
        this.symbolShown = symbolShown;
        this.symbolKind = symbolKind;
        this.wheelKind = wheelKind;
        this.locked = locked;
    }

    public String getColor() {
        return color;
    }

    public int getScalePercent() {
        return scalePercent;
    }

    public boolean isSymbolShown() {
        return symbolShown;
    }

    public String getSymbolKind() {
        return symbolKind;
    }

    public String getWheelKind() {
        return wheelKind;
    }

    public boolean isLocked() {
        return locked;
    }
}
