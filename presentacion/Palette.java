package presentacion;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalog of the color names the simulator can draw.
 *
 * Colors are kept in a dictionary (name -> Color), so asking whether a
 * name is valid or which Color it is takes a single lookup instead of
 * walking through lists. The dictionary remembers the order in which
 * the colors were registered, which is the order SlotMachine(n) uses
 * to pick its n symbols.
 *
 * @author Jhazael and Santiago
 * @version 2.0 (Cycle 4 - 2026-2)
 */
public class Palette {

    // name -> color, in registration order.
    private static final Map<String, Color> COLORS = new LinkedHashMap<String, Color>();

    static {
        COLORS.put("red", new Color(0xFF0000));
        COLORS.put("black", new Color(0x000000));
        COLORS.put("blue", new Color(0x0000FF));
        COLORS.put("yellow", new Color(0xFFFF00));
        COLORS.put("green", new Color(0x00FF00));
        COLORS.put("magenta", new Color(0xFF00FF));
        COLORS.put("white", new Color(0xFFFFFF));
        COLORS.put("orange", new Color(0xFFA500));
        COLORS.put("purple", new Color(0x800080));
        COLORS.put("pink", new Color(0xFFC0CB));
        COLORS.put("brown", new Color(0xA52A2A));
        COLORS.put("cyan", new Color(0x00FFFF));
        COLORS.put("gray", new Color(0x808080));
        COLORS.put("navy", new Color(0x000080));
        COLORS.put("teal", new Color(0x008080));
        COLORS.put("olive", new Color(0x808000));
        COLORS.put("maroon", new Color(0x800000));
        COLORS.put("gold", new Color(0xFFD700));
        COLORS.put("coral", new Color(0xFF7F50));
        COLORS.put("salmon", new Color(0xFA8072));
        COLORS.put("tomato", new Color(0xFF6347));
        COLORS.put("crimson", new Color(0xDC143C));
        COLORS.put("orchid", new Color(0xDA70D6));
        COLORS.put("plum", new Color(0xDDA0DD));
        COLORS.put("violet", new Color(0xEE82EE));
        COLORS.put("indigo", new Color(0x4B0082));
        COLORS.put("khaki", new Color(0xF0E68C));
        COLORS.put("tan", new Color(0xD2B48C));
        COLORS.put("wheat", new Color(0xF5DEB3));
        COLORS.put("beige", new Color(0xF5F5DC));
        COLORS.put("lavender", new Color(0xE6E6FA));
        COLORS.put("turquoise", new Color(0x40E0D0));
        COLORS.put("skyblue", new Color(0x87CEEB));
        COLORS.put("steelblue", new Color(0x4682B4));
        COLORS.put("royalblue", new Color(0x4169E1));
        COLORS.put("dodgerblue", new Color(0x1E90FF));
        COLORS.put("deepskyblue", new Color(0x00BFFF));
        COLORS.put("cadetblue", new Color(0x5F9EA0));
        COLORS.put("seagreen", new Color(0x2E8B57));
        COLORS.put("forestgreen", new Color(0x228B22));
        COLORS.put("limegreen", new Color(0x32CD32));
        COLORS.put("springgreen", new Color(0x00FF7F));
        COLORS.put("chartreuse", new Color(0x7FFF00));
        COLORS.put("yellowgreen", new Color(0x9ACD32));
        COLORS.put("olivedrab", new Color(0x6B8E23));
        COLORS.put("sienna", new Color(0xA0522D));
        COLORS.put("chocolate", new Color(0xD2691E));
        COLORS.put("peru", new Color(0xCD853F));
        COLORS.put("hotpink", new Color(0xFF69B4));
        COLORS.put("deeppink", new Color(0xFF1493));
    }

    /**
     * @return how many different colors the palette has.
     */
    public static int size() {
        return COLORS.size();
    }

    /**
     * @param count how many names are wanted.
     * @return the first count color names of the palette, in order (all of
     * them if count is bigger than the palette; none if it is negative).
     */
    public static List<String> firstNames(int count) {
        List<String> names = new ArrayList<String>(COLORS.keySet());
        return names.subList(0, Math.max(0, Math.min(count, names.size())));
    }

    /**
     * @param name CSS color name to look for.
     * @return true if the palette has a color with that name.
     */
    public static boolean isValid(String name) {
        return COLORS.containsKey(name);
    }

    /**
     * Translates a color name into a java.awt.Color so the Canvas can
     * paint it. Unknown names are painted black.
     *
     * @param name CSS color name.
     * @return the matching Color, or black if the name is unknown.
     */
    public static Color toColor(String name) {
        return COLORS.getOrDefault(name, Color.black);
    }
}
