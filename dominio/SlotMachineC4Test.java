package dominio;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import presentacion.Palette;

/**
 * Unit tests of the whole project for Cycle 4: kinds of wheels, kinds
 * of symbols, the ok() state, spins, jackpot and the marathon solver.
 * Every machine is invisible, so no window or dialog interrupts them.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class SlotMachineC4Test {

    private SlotMachine machine;

    @Before
    public void setUp() {
        machine = new SlotMachine();
        machine.makeInvisible();
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "blue");
        machine.addSymbol(3, "green");
    }

    @After
    public void tearDown() {
        machine.makeInvisible();
    }

    // ---------------------------------------------------------- wheels

    @Test
    public void shouldAddWheelsOfEveryKindIgnoringCase() {
        machine.addWheel("Normal", 1);
        assertTrue(machine.ok());
        machine.addWheel("LEFTY", 2);
        assertTrue(machine.ok());
        machine.addWheel(" rebel ", 3);
        assertTrue(machine.ok());
        machine.addWheel("mirror", 4);
        assertTrue(machine.ok());
        assertEquals(4, machine.configuration().length);
    }

    @Test
    public void shouldRejectUnknownWheelType() {
        machine.addWheel("wobbly", 1);
        assertFalse(machine.ok());
        machine.addWheel(null, 1);
        assertFalse(machine.ok());
        assertEquals(0, machine.configuration().length);
    }

    @Test
    public void shouldBornEveryWheelShowingTheFirstSymbol() {
        machine.addWheel("normal", 1);
        machine.addWheel("mirror", 2);
        assertArrayEquals(new String[]{"red", "red"}, machine.configuration());
    }

    @Test
    public void leftyShouldCopyTheWheelOnItsLeft() {
        machine.addWheel("normal", 1);
        machine.addWheel("lefty", 2);
        machine.spin(1, 1);
        assertEquals("blue", machine.configuration()[0]);
        machine.spin(2, 1);
        assertTrue(machine.ok());
        assertEquals("blue", machine.configuration()[1]);
    }

    @Test
    public void leftyWithoutLeftWheelShouldRotateNormally() {
        machine.addWheel("lefty", 1);
        machine.spin(1, 2);
        assertEquals("green", machine.configuration()[0]);
    }

    @Test
    public void leftyShouldFollowTheNewNeighbourAfterSwap() {
        machine.addWheel("normal", 1);
        machine.addWheel("normal", 2);
        machine.addWheel("lefty", 3);
        machine.spin(2, 2);                     // wheel 2 shows green
        machine.swap(1, 2);                     // now green is wheel 1
        machine.spin(2, 1);                     // wheel 2 shows red -> blue
        machine.spin(3, 1);                     // lefty copies wheel 2
        assertEquals(machine.configuration()[1], machine.configuration()[2]);
    }

    @Test
    public void rebelShouldRefuseLockSwapAndDelete() {
        machine.addWheel("normal", 1);
        machine.addWheel("rebel", 2);

        assertEquals(0, machine.lock(2));
        assertFalse(machine.ok());
        machine.spin(2, 1);
        assertTrue(machine.ok());               // it was not locked

        machine.swap(1, 2);
        assertFalse(machine.ok());
        machine.swap(2, 1);
        assertFalse(machine.ok());

        machine.delWheel(2);
        assertFalse(machine.ok());
        assertEquals(2, machine.configuration().length);
    }

    @Test
    public void normalWheelShouldAcceptLockSwapAndDelete() {
        machine.addWheel("normal", 1);
        machine.addWheel("normal", 2);

        assertEquals(1, machine.lock(1));
        assertTrue(machine.ok());
        machine.swap(1, 2);
        assertTrue(machine.ok());
        machine.delWheel(2);
        assertTrue(machine.ok());
        assertEquals(1, machine.configuration().length);
    }

    @Test
    public void lockShouldCountTheLockedWheels() {
        machine.addWheel("normal", 1);
        machine.addWheel("lefty", 2);
        machine.addWheel("rebel", 3);
        assertEquals(1, machine.lock(1));
        assertEquals(2, machine.lock(2));
        assertEquals(2, machine.lock(3));
        machine.unlock(1);
        assertEquals(1, machine.lock(2));
    }

    @Test
    public void lockOnEmptyMachineShouldFail() {
        assertEquals(0, new SlotMachine().lock(1));
    }

    @Test
    public void mirrorShouldTurnOppositeToWhatItIsAsked() {
        machine.addWheel("mirror", 1);
        machine.spin(1, 1);
        assertEquals("green", machine.configuration()[0]);
        machine.spin(1, -1);
        assertEquals("red", machine.configuration()[0]);
    }

    @Test
    public void lockedWheelShouldNotSpin() {
        machine.addWheel("mirror", 1);
        machine.lock(1);
        machine.spin(1, 1);
        assertFalse(machine.ok());
        assertEquals("red", machine.configuration()[0]);
    }

    // --------------------------------------------------------- symbols

    @Test
    public void shouldAddSymbolsOfEveryKindIgnoringCase() {
        SlotMachine other = new SlotMachine();
        other.addSymbol("Normal", 1, "red");
        assertTrue(other.ok());
        other.addSymbol("EPHEMERAL", 2, "blue");
        assertTrue(other.ok());
        other.addSymbol("shy", 3, "green");
        assertTrue(other.ok());
        other.addSymbol("growing", 4, "yellow");
        assertTrue(other.ok());
        assertArrayEquals(new String[]{"red", "blue", "green", "yellow"},
                          other.symbols());
    }

    @Test
    public void shouldRejectUnknownSymbolTypeOrRepeatedColor() {
        machine.addSymbol("sneaky", 1, "yellow");
        assertFalse(machine.ok());
        machine.addSymbol("shy", 1, "red");     // red already exists
        assertFalse(machine.ok());
        machine.addSymbol("shy", 1, "notacolor");
        assertFalse(machine.ok());
        assertEquals(3, machine.symbols().length);
    }

    @Test
    public void ephemeralSymbolShouldShrinkUntilAPoint() {
        Symbol symbol = new EphemeralSymbol("red");
        assertEquals(100, symbol.scalePercent());
        symbol.selected();
        assertEquals(80, symbol.scalePercent());
        for (int i = 0; i < 20; i++) {
            symbol.selected();
        }
        assertEquals(0, symbol.scalePercent());
        assertTrue(symbol.isShown());
    }

    @Test
    public void shySymbolShouldToggleVisibilityWhenSelected() {
        Symbol symbol = new ShySymbol("red");
        assertTrue(symbol.isShown());
        symbol.selected();
        assertFalse(symbol.isShown());
        symbol.selected();
        assertTrue(symbol.isShown());
        assertEquals(100, symbol.scalePercent());
    }

    @Test
    public void growingSymbolShouldGrowUpToFortyPercentMore() {
        Symbol symbol = new GrowingSymbol("red");
        symbol.selected();
        assertEquals(120, symbol.scalePercent());
        for (int i = 0; i < 20; i++) {
            symbol.selected();
        }
        assertEquals(140, symbol.scalePercent());
    }

    @Test
    public void normalSymbolShouldNeverChange() {
        Symbol symbol = new NormalSymbol("red");
        for (int i = 0; i < 10; i++) {
            symbol.selected();
        }
        assertEquals(100, symbol.scalePercent());
        assertTrue(symbol.isShown());
        assertEquals("red", symbol.getColor());
    }

    @Test
    public void wheelShouldSelectEverySymbolItPasses() {
        ArrayList<Symbol> sequence = new ArrayList<Symbol>();
        Symbol ephemeral = new EphemeralSymbol("red");
        Symbol shy = new ShySymbol("blue");
        sequence.add(ephemeral);
        sequence.add(shy);
        Wheel wheel = new NormalWheel(sequence);

        wheel.rotate(4);                        // blue, red, blue, red

        assertEquals(60, ephemeral.scalePercent());
        assertTrue(shy.isShown());              // toggled twice
        assertEquals("red", wheel.showingColor());
    }

    @Test
    public void wheelShouldKeepShowingTheSameSymbolWhenOneIsAddedBefore() {
        machine.addWheel("normal", 1);
        machine.spin(1, 1);                     // blue
        machine.addSymbol("shy", 1, "yellow");
        assertEquals("blue", machine.configuration()[0]);
    }

    // ------------------------------------------------- ok, spin, jackpot

    @Test
    public void okShouldReflectTheLastOperation() {
        assertTrue(machine.ok());
        machine.spin(1, 1);                     // no wheels
        assertFalse(machine.ok());
        machine.addWheel(1);
        assertTrue(machine.ok());
        machine.placeSymbol(1, "purple");       // no such symbol
        assertFalse(machine.ok());
        machine.placeSymbol(1, "blue");
        assertTrue(machine.ok());
    }

    @Test
    public void spinWithoutSymbolsShouldFail() {
        SlotMachine empty = new SlotMachine();
        empty.addWheel(1);
        empty.spin();
        assertFalse(empty.ok());
    }

    @Test
    public void spinShouldWrapAroundBothDirections() {
        machine.addWheel(1);
        machine.spin(1, 4);
        assertEquals("blue", machine.configuration()[0]);
        machine.spin(1, -2);
        assertEquals("green", machine.configuration()[0]);
    }

    @Test
    public void spinWheelAndSpinAllShouldChangeUnlockedWheels() {
        machine.addWheel(1);
        machine.addWheel(2);
        machine.lock(1);
        machine.spin();
        assertTrue(machine.ok());
        assertEquals("red", machine.configuration()[0]);
        assertNotEquals("red", machine.configuration()[1]);
        machine.spin(2);
        assertTrue(machine.ok());
    }

    @Test
    public void spinConfigurationShouldBeAtomic() {
        machine.addWheel(1);
        machine.addWheel(2);
        machine.spin(new String[]{"green", "blue"});
        assertArrayEquals(new String[]{"green", "blue"}, machine.configuration());
        machine.spin(new String[]{"red", "purple"});
        assertFalse(machine.ok());
        assertArrayEquals(new String[]{"green", "blue"}, machine.configuration());
    }

    @Test
    public void shouldDetectJackpotAndDistinctSymbols() {
        machine.addWheel(1);
        machine.addWheel(2);
        machine.addWheel(3);
        assertTrue(machine.isJackpot());
        assertEquals(1, machine.distinctSymbols());
        machine.spin(2, 1);
        assertFalse(machine.isJackpot());
        assertEquals(2, machine.distinctSymbols());
        machine.placeSymbol(2, "red");
        assertTrue(machine.isJackpot());
    }

    @Test
    public void swapAndDeleteSymbolShouldWork() {
        machine.addWheel(1);
        machine.addWheel(2);
        machine.placeSymbol(2, "blue");
        machine.swap(1, 2);
        assertArrayEquals(new String[]{"blue", "red"}, machine.configuration());
        machine.delSymbol("blue");
        assertTrue(machine.ok());
        assertEquals("green", machine.symbols()[1]);
        machine.delSymbol("blue");
        assertFalse(machine.ok());
    }

    // ------------------------------------------------ SlotMachine(n) / solver

    @Test
    public void randomMachineShouldHaveNWheelsNSymbolsAndNoJackpot() {
        for (int n = 2; n <= 12; n++) {
            SlotMachine random = new SlotMachine(n);
            assertTrue(random.ok());
            assertEquals(n, random.configuration().length);
            assertEquals(n, random.symbols().length);
            assertFalse(random.isJackpot());
        }
    }

    @Test
    public void randomMachineWithInvalidSizeShouldFail() {
        assertFalse(new SlotMachine(0).ok());
        assertFalse(new SlotMachine(51).ok());
    }

    @Test
    public void solverShouldReachJackpotForManySizes() {
        for (int n = 1; n <= 12; n++) {
            for (int attempt = 0; attempt < 20; attempt++) {
                SlotMachine random = new SlotMachine(n);
                int[][] actions = SlotMachineContest.solve(random, n);
                assertTrue("n=" + n, random.isJackpot());
                assertTrue(actions.length <= 10000);
            }
        }
    }

    @Test
    public void solverShouldSolveTheBiggestMachineWithinTheLimit() {
        for (int attempt = 0; attempt < 5; attempt++) {
            SlotMachine random = new SlotMachine(50);
            int[][] actions = SlotMachineContest.solve(random, 50);
            assertTrue(random.isJackpot());
            assertTrue(actions.length <= 10000);
        }
    }

    @Test
    public void solveShouldReturnNothingWhenSizeIsOutOfRange() {
        assertEquals(0, SlotMachineContest.solve(0).length);
        assertEquals(0, SlotMachineContest.solve(51).length);
    }

    // ----------------------------------------- dictionaries (Palette, colors)

    @Test
    public void paletteShouldKnowItsColorsThroughTheDictionary() {
        assertEquals(50, Palette.size());
        assertTrue(Palette.isValid("red"));
        assertTrue(Palette.isValid("deeppink"));
        assertFalse(Palette.isValid("notacolor"));
        assertFalse(Palette.isValid(null));
        assertEquals(java.awt.Color.red, Palette.toColor("red"));
        assertEquals(java.awt.Color.black, Palette.toColor("notacolor"));
    }

    @Test
    public void paletteShouldGiveItsFirstColorsInOrder() {
        List<String> three = Palette.firstNames(3);
        assertEquals(3, three.size());
        assertEquals("red", three.get(0));
        assertEquals(0, Palette.firstNames(-1).size());
        assertEquals(50, Palette.firstNames(99).size());
    }

    @Test
    public void symbolsShouldBeFoundByColorAfterAddingAndDeleting() {
        machine.addWheel(1);
        machine.placeSymbol(1, "green");
        assertTrue(machine.ok());
        machine.delSymbol("green");            // the wheel was showing it
        assertTrue(machine.ok());
        machine.placeSymbol(1, "green");       // it is gone
        assertFalse(machine.ok());
        assertNotNull(machine.configuration()[0]);
        machine.addSymbol("shy", 3, "green");  // the color can be used again
        assertTrue(machine.ok());
        machine.placeSymbol(1, "green");
        assertTrue(machine.ok());
        assertEquals("green", machine.configuration()[0]);
    }

    @Test
    public void randomMachineShouldUseTheFirstColorsOfThePalette() {
        SlotMachine random = new SlotMachine(5);
        assertArrayEquals(Palette.firstNames(5).toArray(new String[0]),
                          random.symbols());
    }
}
