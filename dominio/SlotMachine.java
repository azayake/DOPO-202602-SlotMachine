package dominio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import presentacion.MachineView;
import presentacion.Palette;
import presentacion.WheelFigure;

/**
 * This class represents the simulator of a slot machine.
 * A slot machine has several wheels (Wheel objects) and a sequence
 * of colored symbols (Symbol objects).
 *
 * Design decision: there is only ONE symbol sequence and it belongs
 * to the machine, not to each wheel (symbols appear in the same order
 * on every wheel). What distinguishes one wheel from another is not
 * its symbols, but which of them it is showing.
 *
 * Wheels (normal, lefty, rebel, mirror) and symbols (normal,
 * ephemeral, shy, growing) come in several kinds, chosen by name when
 * they are added. The machine never checks the kind: each wheel and
 * symbol knows how to behave. Kinds are found in two dictionaries
 * (name -> constructor) and symbols are found by color in a third one
 * (color -> symbol), so no list is ever walked comparing names.
 *
 * The machine knows nothing about drawing: when it is visible it only
 * sends its state to the MachineView of the presentation package.
 * Since Cycle 3 it also implements TestingTool.
 *
 * @author Jhazael and Santiago
 * @version 5.0 (Cycle 4 - 2026-2)
 */
public class SlotMachine implements TestingTool {

    // Milliseconds each step of a spin takes when the machine is visible.
    private static final int STEP_PAUSE = 150;

    // Kinds of wheels and symbols, by name: a new kind is a new entry.
    private static final Map<String, Function<List<Symbol>, Wheel>> WHEEL_TYPES =
            new HashMap<String, Function<List<Symbol>, Wheel>>();
    private static final Map<String, Function<String, Symbol>> SYMBOL_TYPES =
            new HashMap<String, Function<String, Symbol>>();

    static {
        WHEEL_TYPES.put(NormalWheel.KIND, NormalWheel::new);
        WHEEL_TYPES.put(LeftyWheel.KIND, LeftyWheel::new);
        WHEEL_TYPES.put(RebelWheel.KIND, RebelWheel::new);
        WHEEL_TYPES.put(MirrorWheel.KIND, MirrorWheel::new);
        SYMBOL_TYPES.put(NormalSymbol.KIND, NormalSymbol::new);
        SYMBOL_TYPES.put(EphemeralSymbol.KIND, EphemeralSymbol::new);
        SYMBOL_TYPES.put(ShySymbol.KIND, ShySymbol::new);
        SYMBOL_TYPES.put(GrowingSymbol.KIND, GrowingSymbol::new);
    }

    // Symbol sequence of the machine, in order. All wheels share it.
    private List<Symbol> symbols;

    // The same symbols, found by color.
    private Map<String, Symbol> symbolsByColor;

    // All the wheels the machine currently has, from left to right.
    private List<Wheel> wheels;

    // Screen of the machine; created the first time it is made visible.
    private MachineView view;
    private boolean isVisible;

    // Indicates whether the last operation performed was successful.
    private boolean successfulOperation;

    // Used to rotate wheels a random number of steps.
    private Random random;

    /**
     * Constructor. Creates a machine with no wheels, no symbols and
     * in invisible mode, as required by the assignment.
     */
    public SlotMachine() {
        symbols = new ArrayList<Symbol>();
        symbolsByColor = new HashMap<String, Symbol>();
        wheels = new ArrayList<Wheel>();
        isVisible = false;
        successfulOperation = true;
        random = new Random();
    }

    /**
     * Constructor for the marathon problem. Creates a machine with n
     * wheels and n different symbols (the first n colors of the
     * Palette), and leaves every wheel showing a random symbol. As in
     * the marathon statement, the initial configuration is never a
     * jackpot (when n is 2 or more). The machine is invisible.
     *
     * If n is smaller than 1 or bigger than the number of colors in
     * the Palette, there are not n different symbols to build the
     * machine: it stays empty and ok() returns false.
     *
     * @param n number of wheels and number of symbols.
     */
    public SlotMachine(int n) {
        this();

        if (n < 1 || n > Palette.size()) {
            successfulOperation = false;
            return;
        }

        List<String> colors = Palette.firstNames(n);
        for (int i = 0; i < n; i++) {
            addSymbol(i + 1, colors.get(i));
        }
        for (int i = 1; i <= n; i++) {
            addWheel(i);
        }

        // spin every wheel to a random symbol; if all ended up equal
        // (a jackpot) spin again, because the problem guarantees that
        // the machine does not start already won
        spinAllWheelsRandomly();
        while (n > 1 && isJackpot()) {
            spinAllWheelsRandomly();
        }
        successfulOperation = true;
    }

    /**
     * Adds a new normal wheel at the indicated position. The wheel is
     * born showing the first symbol of the machine's sequence. If the
     * position is out of range it is adjusted to the nearest allowed end.
     *
     * @param pos position where the wheel should be added (starts at 1).
     */
    public void addWheel(int pos) {
        addWheel(NormalWheel.KIND, pos);
    }

    /**
     * Adds a new wheel of the indicated kind at the indicated position.
     * Fails if the kind does not exist.
     *
     * @param type kind of wheel: "normal", "lefty", "rebel" or "mirror" (any case).
     * @param pos  position where the wheel should be added (starts at 1).
     */
    public void addWheel(String type, int pos) {
        Function<List<Symbol>, Wheel> maker = WHEEL_TYPES.get(normalize(type));
        if (maker == null) {
            fail("There is no wheel of type " + type + ".");
            return;
        }
        wheels.add(adjustPosition(pos, wheels.size() + 1), maker.apply(symbols));
        succeed();
    }

    /**
     * Removes the wheel at the indicated position. A rebel wheel
     * refuses and the operation fails.
     *
     * @param pos position of the wheel to remove (starts at 1).
     */
    public void delWheel(int pos) {
        if (wheels.isEmpty()) {
            fail("There are no wheels to remove.");
            return;
        }
        int index = adjustPosition(pos, wheels.size());
        if (!wheels.get(index).canBeDeleted()) {
            fail("This wheel refuses to be deleted.");
            return;
        }
        wheels.remove(index);
        succeed();
    }

    /**
     * Locks a wheel. A rebel wheel refuses and the operation fails.
     *
     * @param wheel position of the wheel to lock (starts at 1).
     * @return how many wheels are locked after the operation.
     */
    public int lock(int wheel) {
        if (wheels.isEmpty()) {
            fail("Cannot lock a wheel when there are no wheels.");
            return 0;
        }
        Wheel target = wheels.get(adjustPosition(wheel, wheels.size()));
        if (!target.canBeLocked()) {
            fail("This wheel refuses to be locked.");
            return lockedCount();
        }
        target.lockWheel();
        succeed();
        return lockedCount();
    }

    /**
     * Unlocks a wheel.
     *
     * @param wheel position of the wheel to unlock (starts at 1).
     */
    public void unlock(int wheel) {
        if (wheels.isEmpty()) {
            fail("Cannot unlock a wheel when there are no wheels.");
            return;
        }
        wheels.get(adjustPosition(wheel, wheels.size())).unlockWheel();
        succeed();
    }

    /**
     * Rotates a specific wheel the indicated number of steps, one step
     * at a time (so every symbol it passes reacts). If the wheel is
     * locked, the operation fails.
     *
     * @param wheel position of the wheel to rotate (starts at 1).
     * @param steps how many steps the wheel rotates; negative values rotate backwards.
     */
    public void spin(int wheel, int steps) {
        int index = spinnableWheel(wheel);
        if (index < 0) {
            return;
        }

        int direction = steps >= 0 ? 1 : -1;
        for (int i = 0; i < Math.abs(steps); i++) {
            wheels.get(index).spin(direction, leftOf(index));
            if (isVisible) {
                view.show(figures(), isJackpot());
                view.pause(STEP_PAUSE);
            }
        }
        succeed();
    }

    /**
     * Leaves the machine in the indicated configuration: each wheel
     * shows the symbol of the color that corresponds to it in the array.
     * The operation is atomic: if something does not add up, nothing
     * is changed.
     *
     * @param setSymbols colors that each wheel should show, from left to right.
     */
    public void spin(String[] setSymbols) {
        if (setSymbols == null) {
            fail("No configuration was received.");
            return;
        }
        if (setSymbols.length != wheels.size()) {
            fail("The configuration does not have one color per wheel.");
            return;
        }
        for (int i = 0; i < setSymbols.length; i++) {
            if (!symbolsByColor.containsKey(setSymbols[i])) {
                fail("The machine has no symbol of color " + setSymbols[i] + ".");
                return;
            }
            if (wheels.get(i).isLocked()
                    && !setSymbols[i].equals(wheels.get(i).showingColor())) {
                fail("A locked wheel would have to change its symbol.");
                return;
            }
        }

        for (int i = 0; i < wheels.size(); i++) {
            wheels.get(i).place(symbolsByColor.get(setSymbols[i]));
        }
        succeed();
    }

    /**
     * Exchanges two wheels. It fails if there are fewer than two
     * wheels, if both positions are the same wheel, or if any of the
     * two wheels is a rebel (rebels refuse to be swapped).
     *
     * @param wheel1 first wheel (starts at 1; if out of range it is adjusted to the nearest end).
     * @param wheel2 second wheel (same rule).
     */
    public void swap(int wheel1, int wheel2) {
        if (wheels.size() <= 1) {
            fail("At least two wheels are needed to perform a swap.");
            return;
        }
        int position1 = adjustPosition(wheel1, wheels.size());
        int position2 = adjustPosition(wheel2, wheels.size());
        if (position1 == position2) {
            fail("Cannot swap two wheels that are in the same position.");
            return;
        }
        if (!wheels.get(position1).canBeSwapped()
                || !wheels.get(position2).canBeSwapped()) {
            fail("A rebel wheel refuses to be swapped.");
            return;
        }
        Wheel temp = wheels.get(position1);
        wheels.set(position1, wheels.get(position2));
        wheels.set(position2, temp);
        succeed();
    }

    /**
     * Adds a normal symbol of the indicated color to the machine's
     * sequence, at the requested position.
     *
     * @param pos   position where the symbol is wanted (starts at 1).
     * @param color CSS color of the symbol, for example "red" or "blue".
     */
    public void addSymbol(int pos, String color) {
        addSymbol(NormalSymbol.KIND, pos, color);
    }

    /**
     * Adds a symbol of the indicated kind and color to the machine's
     * sequence. It fails if the kind does not exist, if the color is
     * not in the Palette or if a symbol of that color already exists.
     *
     * @param type  kind of symbol: "normal", "ephemeral", "shy" or "growing" (any case).
     * @param pos   position where the symbol is wanted (starts at 1).
     * @param color CSS color of the symbol, for example "red" or "blue".
     */
    public void addSymbol(String type, int pos, String color) {
        Function<String, Symbol> maker = SYMBOL_TYPES.get(normalize(type));
        if (maker == null) {
            fail("There is no symbol of type " + type + ".");
            return;
        }
        if (!Palette.isValid(color)) {
            fail("The color " + color + " cannot be drawn.");
            return;
        }
        if (symbolsByColor.containsKey(color)) {
            fail("A symbol of that color already exists.");
            return;
        }

        Symbol symbol = maker.apply(color);
        symbols.add(adjustPosition(pos, symbols.size() + 1), symbol);
        symbolsByColor.put(color, symbol);
        refreshWheels();
        succeed();
    }

    /**
     * Removes from the machine's sequence the symbol with the
     * indicated color.
     *
     * @param symbol color of the symbol to remove.
     */
    public void delSymbol(String symbol) {
        Symbol removed = symbolsByColor.remove(symbol);
        if (removed == null) {
            fail("There is no symbol of that color.");
            return;
        }
        symbols.remove(removed);
        refreshWheels();
        succeed();
    }

    /**
     * Fixes, on the indicated wheel, the symbol of the indicated
     * color as the symbol being shown. If the wheel is locked, the
     * operation fails.
     *
     * @param wheel  position of the wheel (starts at 1).
     * @param symbol color of the symbol to be shown.
     */
    public void placeSymbol(int wheel, String symbol) {
        if (wheels.isEmpty()) {
            fail("There are no wheels configured.");
            return;
        }
        Wheel target = wheels.get(adjustPosition(wheel, wheels.size()));
        if (target.isLocked()) {
            fail("The wheel is locked and its symbol cannot be changed.");
            return;
        }
        Symbol chosen = symbolsByColor.get(symbol);
        if (chosen == null) {
            fail("The machine has no symbol of that color.");
            return;
        }
        target.place(chosen);
        succeed();
    }

    /**
     * Rotates a single wheel of the machine a random number of steps
     * (always at least one, so its symbol changes). If the wheel is
     * locked, the operation fails.
     *
     * @param wheel position of the wheel to rotate (starts at 1).
     */
    public void spin(int wheel) {
        int index = spinnableWheel(wheel);
        if (index < 0) {
            return;
        }
        wheels.get(index).spin(randomSteps(), leftOf(index));
        succeed();
    }

    /**
     * Rotates all the wheels of the machine, one by one.
     * Locked wheels are skipped and the operation still succeeds.
     */
    public void spin() {
        if (!hasSomethingToSpin()) {
            return;
        }
        for (int i = 0; i < wheels.size(); i++) {
            if (!wheels.get(i).isLocked()) {
                wheels.get(i).spin(randomSteps(), leftOf(i));
            }
        }
        succeed();
    }

    /**
     * @return the colors of the machine's symbols, in sequence order
     * (starting at position 1).
     */
    public String[] symbols() {
        String[] colors = new String[symbols.size()];
        for (int i = 0; i < symbols.size(); i++) {
            colors[i] = symbols.get(i).getColor();
        }
        successfulOperation = true;
        return colors;
    }

    /**
     * @return the number of distinct colors currently being shown on
     * the wheels. It is the k from the original problem: if it is 1,
     * all the wheels are showing the same thing.
     */
    public int distinctSymbols() {
        successfulOperation = true;
        return distinctCount();
    }

    /**
     * @return the color each wheel is currently showing, ordered
     * from left to right.
     */
    public String[] configuration() {
        String[] colors = new String[wheels.size()];
        for (int i = 0; i < wheels.size(); i++) {
            colors[i] = wheels.get(i).showingColor();
        }
        successfulOperation = true;
        return colors;
    }

    /**
     * @return true if the machine is in a winning configuration, that
     * is, if all the wheels show the same symbol.
     */
    public boolean isJackpot() {
        return !wheels.isEmpty() && distinctCount() == 1;
    }

    /**
     * Makes the whole simulator visible.
     */
    public void makeVisible() {
        if (view == null) {
            view = new MachineView();
        }
        isVisible = true;
        succeed();
    }

    /**
     * Makes the whole simulator invisible.
     */
    public void makeInvisible() {
        isVisible = false;
        if (view != null) {
            view.hide();
        }
        successfulOperation = true;
    }

    /**
     * Ends the simulator. For now it simply hides everything.
     */
    public void exit() {
        makeInvisible();
    }

    /**
     * @return true if the last operation invoked could be performed
     * correctly, false otherwise.
     */
    public boolean ok() {
        return successfulOperation;
    }

    /**
     * Rotates every wheel a random number of steps (used by the
     * constructor SlotMachine(n) to leave the machine random).
     */
    private void spinAllWheelsRandomly() {
        for (Wheel wheel : wheels) {
            wheel.rotate(random.nextInt(symbols.size()));
        }
    }

    /**
     * @return a random number of steps for spin() and spin(wheel). It
     * is never 0 (and never a whole turn), so a spun wheel always ends
     * showing a different symbol when the machine has two or more.
     */
    private int randomSteps() {
        if (symbols.size() <= 1) {
            return 0;
        }
        return 1 + random.nextInt(symbols.size() - 1);
    }

    /**
     * Converts a position given by the user into a list index. As the
     * assignment requires, a value out of range is moved to the nearest
     * end instead of being rejected.
     *
     * @param pos     position requested by the user (starts at 1).
     * @param maximum largest position that is accepted.
     * @return the equivalent index in the list (starts at 0).
     */
    private int adjustPosition(int pos, int maximum) {
        return Math.max(1, Math.min(pos, maximum)) - 1;
    }

    /**
     * @param type kind of wheel or symbol, as written by the user.
     * @return the key used in the kind dictionaries (lower case, no
     * blanks around it); an empty text if there is no name.
     */
    private static String normalize(String type) {
        return type == null ? "" : type.trim().toLowerCase();
    }

    /**
     * @return how many different colors the wheels are showing.
     */
    private int distinctCount() {
        Set<String> seen = new HashSet<String>();
        for (Wheel wheel : wheels) {
            if (wheel.showingColor() != null) {
                seen.add(wheel.showingColor());
            }
        }
        return seen.size();
    }

    /**
     * Checks that the machine can spin at all.
     *
     * @return true if there are wheels and symbols; otherwise the
     * operation is marked as failed and false is returned.
     */
    private boolean hasSomethingToSpin() {
        if (wheels.isEmpty()) {
            fail("There are no wheels to spin.");
            return false;
        }
        if (symbols.isEmpty()) {
            fail("The machine has no symbols configured.");
            return false;
        }
        return true;
    }

    /**
     * Checks that a wheel can be spun and finds where it is.
     *
     * @param wheel position of the wheel (starts at 1).
     * @return the index of the wheel in the list, or -1 (and the
     * operation fails) if the machine cannot spin or the wheel is locked.
     */
    private int spinnableWheel(int wheel) {
        if (!hasSomethingToSpin()) {
            return -1;
        }
        int index = adjustPosition(wheel, wheels.size());
        if (wheels.get(index).isLocked()) {
            fail("The wheel is locked and cannot be spun.");
            return -1;
        }
        return index;
    }

    /**
     * @param index index of a wheel in the list.
     * @return the wheel placed on its left, or null if it is the first.
     */
    private Wheel leftOf(int index) {
        return index > 0 ? wheels.get(index - 1) : null;
    }

    /**
     * @return how many wheels are locked right now.
     */
    private int lockedCount() {
        int count = 0;
        for (Wheel wheel : wheels) {
            if (wheel.isLocked()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Tells all wheels that the symbol sequence changed, so they can
     * adjust what they are showing.
     */
    private void refreshWheels() {
        for (Wheel wheel : wheels) {
            wheel.refresh();
        }
    }

    /**
     * @return one figure per wheel, with what the screen needs to draw it.
     */
    private List<WheelFigure> figures() {
        List<WheelFigure> figures = new ArrayList<WheelFigure>();
        for (Wheel wheel : wheels) {
            Symbol shown = wheel.showing();
            figures.add(new WheelFigure(
                    wheel.showingColor(),
                    shown == null ? 100 : shown.scalePercent(),
                    shown != null && shown.isShown(),
                    shown == null ? NormalSymbol.KIND : shown.kind(),
                    wheel.kind(),
                    wheel.isLocked()));
        }
        return figures;
    }

    /**
     * Marks the last operation as successful and, if the machine is
     * visible, redraws it.
     */
    private void succeed() {
        successfulOperation = true;
        if (isVisible) {
            view.show(figures(), isJackpot());
        }
    }

    /**
     * Marks the last operation as failed and warns the user, but ONLY
     * if the simulator is visible. If it is invisible, the operation
     * fails silently (usability requirement).
     *
     * @param message text to be shown to the user.
     */
    private void fail(String message) {
        successfulOperation = false;
        if (isVisible) {
            view.warn(message);
        }
    }
}
