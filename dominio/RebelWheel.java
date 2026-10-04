package dominio;

import java.util.List;

/**
 * A rebel wheel: it refuses to be locked, swapped or deleted.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class RebelWheel extends Wheel {

    /** Name of this kind of wheel. */
    public static final String KIND = "rebel";

    /**
     * @param sequence symbol list shared with the machine.
     */
    public RebelWheel(List<Symbol> sequence) {
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
     * @return false: a rebel wheel cannot be locked.
     */
    @Override
    public boolean canBeLocked() {
        return false;
    }

    /**
     * @return false: a rebel wheel cannot be swapped.
     */
    @Override
    public boolean canBeSwapped() {
        return false;
    }

    /**
     * @return false: a rebel wheel cannot be deleted.
     */
    @Override
    public boolean canBeDeleted() {
        return false;
    }
}
