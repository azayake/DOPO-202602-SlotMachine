package presentacion;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * The screen of the slot machine: it draws the wheels on the Canvas,
 * shows the warning dialogs and waits between animation steps. It has
 * NO rules of the game; the domain tells it what to draw.
 *
 * Usability: every kind of element looks different.
 *  - the SHAPE of the window tells the kind of symbol it shows
 *    (rectangle = normal, circle = ephemeral, triangle = shy,
 *    diamond = growing);
 *  - the MARK under the window tells the kind of wheel
 *    (black bar = normal, blue arrow to the left = lefty,
 *    red diamond = rebel, green circle = mirror);
 *  - a locked wheel gets a dark bar under its mark.
 * Both are dictionaries (kind -> look), so a new kind of symbol or
 * wheel only needs a new entry; an unknown kind is drawn like a normal one.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class MachineView {

    /** Builds the outline of something that fits in the box (x, y, width, height). */
    private interface Outline {
        Shape make(int x, int y, int width, int height);
    }

    /** Look of a wheel mark: its color and its outline. */
    private static class Mark {
        private final String color;
        private final Outline outline;

        Mark(String color, Outline outline) {
            this.color = color;
            this.outline = outline;
        }
    }

    private static final Outline RECTANGLE = (x, y, w, h) -> new java.awt.Rectangle(x, y, w, h);
    private static final Outline CIRCLE = (x, y, w, h) -> new Ellipse2D.Double(x, y, w, h);
    private static final Outline TRIANGLE = (x, y, w, h) -> new Polygon(
            new int[]{x + w / 2, x, x + w}, new int[]{y, y + h, y + h}, 3);
    private static final Outline LEFT_ARROW = (x, y, w, h) -> new Polygon(
            new int[]{x, x + w, x + w}, new int[]{y + h / 2, y, y + h}, 3);
    private static final Outline DIAMOND = (x, y, w, h) -> new Polygon(
            new int[]{x + w / 2, x + w, x + w / 2, x},
            new int[]{y, y + h / 2, y + h, y + h / 2}, 4);

    // symbol kind -> outline of its window
    private static final Map<String, Outline> SYMBOL_SHAPES = new HashMap<String, Outline>();

    // wheel kind -> mark under its window
    private static final Map<String, Mark> WHEEL_MARKS = new HashMap<String, Mark>();

    static {
        SYMBOL_SHAPES.put("normal", RECTANGLE);
        SYMBOL_SHAPES.put("ephemeral", CIRCLE);
        SYMBOL_SHAPES.put("shy", TRIANGLE);
        SYMBOL_SHAPES.put("growing", DIAMOND);

        WHEEL_MARKS.put("normal", new Mark("black", RECTANGLE));
        WHEEL_MARKS.put("lefty", new Mark("royalblue", LEFT_ARROW));
        WHEEL_MARKS.put("rebel", new Mark("crimson", DIAMOND));
        WHEEL_MARKS.put("mirror", new Mark("seagreen", CIRCLE));
    }

    // Layout (pixels). With many wheels the space shrinks to fit the canvas.
    private static final int SPACE_BETWEEN_WHEELS = 60;
    private static final int CANVAS_USABLE_WIDTH = 840;
    private static final int WHEEL_WIDTH = 40;
    private static final int WHEEL_HEIGHT = 30;
    private static final int FIRST_X = 40;
    private static final int FIRST_Y = 40;
    private static final int MARK_HEIGHT = 10;
    private static final int MARK_WIDTH = 16;
    private static final int MARK_GAP = 8;
    private static final int BODY_MARGIN = 15;
    private static final int BODY_HEIGHT = 80;

    // The body of the machine, drawn behind the wheels.
    private final Rectangle body;

    // Each wheel uses three keys of the Canvas: window, mark and lock bar.
    private final List<Object[]> keys = new java.util.ArrayList<Object[]>();

    /**
     * Creates the view. Nothing is drawn until show() is called.
     */
    public MachineView() {
        body = new Rectangle();
        body.moveHorizontal(FIRST_X - BODY_MARGIN - 70);
        body.moveVertical(FIRST_Y - BODY_MARGIN - 15);
    }

    /**
     * Draws the machine: the body (yellow when it is a jackpot, gray
     * otherwise) and then every wheel from left to right.
     *
     * @param figures one figure per wheel, from left to right.
     * @param jackpot true if the machine is in a winning configuration.
     */
    public void show(List<WheelFigure> figures, boolean jackpot) {
        int space = SPACE_BETWEEN_WHEELS;
        if (!figures.isEmpty() && figures.size() * space > CANVAS_USABLE_WIDTH) {
            space = CANVAS_USABLE_WIDTH / figures.size();
        }
        int width = Math.min(WHEEL_WIDTH, space * 2 / 3);

        // body first, so everything drawn later stays on top of it
        body.changeColor(jackpot ? "yellow" : "gray");
        body.changeSize(BODY_HEIGHT, figures.size() * space + 10);
        body.makeVisible();

        for (int i = 0; i < figures.size(); i++) {
            drawWheel(i, figures.get(i), FIRST_X + i * space, width);
        }
        forgetWheelsFrom(figures.size());
    }

    /**
     * Removes the machine from the screen.
     */
    public void hide() {
        forgetWheelsFrom(0);
        body.makeInvisible();
    }

    /**
     * Shows a warning dialog.
     *
     * @param message text for the user.
     */
    public void warn(String message) {
        JOptionPane.showMessageDialog(null, message, "Slot Machine",
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Waits a moment, so the user can follow the animation.
     *
     * @param milliseconds how long to wait.
     */
    public void pause(int milliseconds) {
        Canvas.getCanvas().wait(milliseconds);
    }

    /**
     * Draws the window, the mark and (if locked) the lock bar of a wheel.
     */
    private void drawWheel(int index, WheelFigure figure, int x, int width) {
        Object[] wheelKeys = keysOf(index);
        Canvas canvas = Canvas.getCanvas();

        // window: centered in its cell, scaled by the size of the symbol
        canvas.erase(wheelKeys[0]);
        if (figure.getColor() != null && figure.isSymbolShown()) {
            int w = Math.max(1, width * figure.getScalePercent() / 100);
            int h = Math.max(1, WHEEL_HEIGHT * figure.getScalePercent() / 100);
            Outline outline = SYMBOL_SHAPES.getOrDefault(figure.getSymbolKind(), RECTANGLE);
            canvas.draw(wheelKeys[0], figure.getColor(),
                    outline.make(x + (width - w) / 2, FIRST_Y + (WHEEL_HEIGHT - h) / 2, w, h));
        }

        // mark of the kind of wheel, under the window
        Mark mark = WHEEL_MARKS.getOrDefault(figure.getWheelKind(), WHEEL_MARKS.get("normal"));
        int markWidth = Math.min(MARK_WIDTH, width);
        int markY = FIRST_Y + WHEEL_HEIGHT + MARK_GAP;
        canvas.draw(wheelKeys[1], mark.color,
                mark.outline.make(x + (width - markWidth) / 2, markY, markWidth, MARK_HEIGHT));

        // lock bar
        canvas.erase(wheelKeys[2]);
        if (figure.isLocked()) {
            canvas.draw(wheelKeys[2], "black", new java.awt.Rectangle(
                    x + (width - markWidth) / 2, markY + MARK_HEIGHT + 3, markWidth, 3));
        }
    }

    /**
     * @return the three Canvas keys of a wheel, creating them the first time.
     */
    private Object[] keysOf(int index) {
        while (keys.size() <= index) {
            keys.add(new Object[]{new Object(), new Object(), new Object()});
        }
        return keys.get(index);
    }

    /**
     * Erases the wheels from the given index on (when there are fewer wheels than before).
     */
    private void forgetWheelsFrom(int first) {
        Canvas canvas = Canvas.getCanvas();
        for (int i = first; i < keys.size(); i++) {
            for (Object key : keys.get(i)) {
                canvas.erase(key);
            }
        }
    }
}
