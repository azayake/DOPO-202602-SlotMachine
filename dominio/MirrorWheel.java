package dominio;

import java.util.List;

/**
 * Custom wheel of Cycle 4. A mirror wheel always turns the opposite way to the one it was asked to: spin it forwards and it goes backwards.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class MirrorWheel extends Wheel {

    /** Name of this kind of wheel. */
    public static final String KIND = "mirror";

    /**
     * @param sequence symbol list shared with the machine.
     */
    public MirrorWheel(List<Symbol> sequence) {
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
     * Turns the wheel in the opposite direction.
     *
     * @param steps how many positions to turn.
     * @param left  the wheel placed on the left of this one, or null.
     */
    @Override
    public void spin(int steps, Wheel left) {
        super.spin(-steps, left);
    }
}
