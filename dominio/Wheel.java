package dominio;

import java.util.List;

/**
 * A wheel of the slot machine. Wheel is the root of the wheel
 * hierarchy (NormalWheel, LeftyWheel, RebelWheel, MirrorWheel): each
 * subclass only changes the small methods that make it different, so
 * the machine treats every wheel in the same way.
 *
 * The wheel does NOT own the symbols: all wheels share the sequence of
 * the machine and each one only remembers which of them it is showing.
 * It does not know how to draw itself either; that is the job of the
 * presentation package.
 *
 * @author Jhazael and Santiago
 * @version 3.0 (Cycle 4 - 2026-2)
 */
public abstract class Wheel {

    // Symbol sequence shared with the machine (the wheel never changes it).
    private List<Symbol> sequence;

    // Position, within the sequence, of the symbol being shown.
    private int shownIndex;

    // Symbol being shown (null while the sequence is empty).
    private Symbol shownSymbol;

    private boolean locked;

    /**
     * Creates an unlocked wheel that reads the indicated symbol
     * sequence and shows its first symbol.
     *
     * @param sequence symbol list shared with the machine.
     */
    public Wheel(List<Symbol> sequence) {
        this.sequence = sequence;
        refresh();
    }

    /**
     * @return the name of this kind of wheel ("normal", "lefty", ...).
     */
    public abstract String kind();

    /**
     * @return true if this kind of wheel accepts to be locked.
     */
    public boolean canBeLocked() {
        return true;
    }

    /**
     * @return true if this kind of wheel accepts to be swapped.
     */
    public boolean canBeSwapped() {
        return true;
    }

    /**
     * @return true if this kind of wheel accepts to be deleted.
     */
    public boolean canBeDeleted() {
        return true;
    }

    /**
     * Locks the wheel. The machine asks canBeLocked() first.
     */
    public void lockWheel() {
        locked = true;
    }

    /**
     * Unlocks the wheel.
     */
    public void unlockWheel() {
        locked = false;
    }

    /**
     * @return true if the wheel is currently locked.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Turns the wheel as the machine asks. This is the method each
     * kind of wheel may change: a normal wheel just rotates.
     *
     * @param steps how many positions to turn (negative = backwards).
     * @param left  the wheel placed on the left of this one, or null.
     */
    public void spin(int steps, Wheel left) {
        rotate(steps);
    }

    /**
     * Rotates the wheel one position at a time, so every symbol it
     * passes is selected. When it goes past the end of the sequence
     * it wraps around, because the wheel is circular.
     *
     * @param steps how many positions to move (negative = backwards).
     */
    public void rotate(int steps) {
        if (sequence.isEmpty()) {
            return;
        }
        int direction = steps >= 0 ? 1 : -1;
        int count = sequence.size();
        for (int i = 0; i < Math.abs(steps); i++) {
            shownIndex = (shownIndex + direction + count) % count;
            select();
        }
    }

    /**
     * Makes this wheel show the same symbol as another wheel.
     *
     * @param other wheel to copy.
     */
    public void copy(Wheel other) {
        shownIndex = other.shownIndex;
        select();
    }

    /**
     * Fixes, as the shown symbol, the indicated one.
     *
     * @param symbol symbol of the machine's sequence to be shown.
     */
    public void place(Symbol symbol) {
        shownIndex = sequence.indexOf(symbol);
        select();
    }

    /**
     * @return the symbol being shown, or null if there are no symbols yet.
     */
    public Symbol showing() {
        return shownSymbol;
    }

    /**
     * @return the color this wheel is showing, or null if there are no
     * symbols yet.
     */
    public String showingColor() {
        return shownSymbol == null ? null : shownSymbol.getColor();
    }

    /**
     * The machine calls this when the symbol sequence changes, so the
     * wheel keeps showing the same symbol (or the nearest position if
     * that symbol was deleted).
     */
    public void refresh() {
        if (sequence.isEmpty()) {
            shownIndex = 0;
            shownSymbol = null;
            return;
        }
        int found = sequence.indexOf(shownSymbol);
        shownIndex = found >= 0 ? found : Math.min(shownIndex, sequence.size() - 1);
        shownSymbol = sequence.get(shownIndex);
    }

    /**
     * The wheel has just landed on a new symbol: the symbol reacts.
     */
    private void select() {
        shownSymbol = sequence.get(shownIndex);
        shownSymbol.selected();
    }
}
