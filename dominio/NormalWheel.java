package dominio;

import java.util.List;

/**
 * The standard wheel: it simply rotates.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class NormalWheel extends Wheel {

    /** Name of this kind of wheel. */
    public static final String KIND = "normal";

    /**
     * @param sequence symbol list shared with the machine.
     */
    public NormalWheel(List<Symbol> sequence) {
        super(sequence);
    }

    /**
     * @return the name of this kind of wheel.
     */
    @Override
    public String kind() {
        return KIND;
    }

}
