package dominio;

import java.util.List;

/**
 * A lefty wheel: when it is spun and there is a wheel on its left, it copies the symbol that wheel is showing instead of rotating. The first wheel of the machine has nothing on its left, so it rotates normally.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class LeftyWheel extends Wheel {

    /** Name of this kind of wheel. */
    public static final String KIND = "lefty";

    /**
     * @param sequence symbol list shared with the machine.
     */
    public LeftyWheel(List<Symbol> sequence) {
        super(sequence);
    }

    /**
     * @return the name of this kind of wheel.
     */
    @Override
    public String kind() {
        return KIND;
    }

    /**
     * Copies the wheel on the left, if there is one.
     *
     * @param steps how many positions to turn (used only without a left wheel).
     * @param left  the wheel placed on the left of this one, or null.
     */
    @Override
    public void spin(int steps, Wheel left) {
        if (left == null) {
            super.spin(steps, left);
        } else {
            copy(left);
        }
    }
}
