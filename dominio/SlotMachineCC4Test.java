package dominio;

import static org.junit.Assert.*;
import org.junit.Assume;
import org.junit.Test;

import java.awt.GraphicsEnvironment;

/**
 * Shared unit tests of Cycle 4 (published for the other groups). They
 * only use the public methods of the assignment, and every machine is
 * invisible except in the simulate case, which needs a screen and is
 * skipped when there is none.
 *
 * @author Jhazael and Santiago
 * @version 1.0 (Cycle 4 - 2026-2)
 */
public class SlotMachineCC4Test {

    /**
     * Every action of a small solution must be a pair {wheel, steps}
     * with the wheel between 1 and n, within the 10 000 actions limit.
     */
    @Test
    public void accordingCjPmShouldSolveSmallMachineWithValidActions() {
        int n = 4;
        int[][] actions = SlotMachineContest.solve(n);

        assertTrue(actions.length <= 10000);
        for (int[] action : actions) {
            assertEquals(2, action.length);
            assertTrue(action[0] >= 1 && action[0] <= n);
        }
    }

    /**
     * The biggest machine of the marathon (50 wheels) must be solved
     * with at most 10 000 actions.
     */
    @Test
    public void accordingCjPmShouldSolveTheBiggestMachineWithinTheLimit() {
        int[][] actions = SlotMachineContest.solve(50);

        assertTrue(actions.length > 0);
        assertTrue(actions.length <= 10000);
    }

    /**
     * A rebel wheel must refuse to be locked, swapped and deleted, and
     * every refusal must be reported through ok().
     */
    @Test
    public void accordingCjPmRebelWheelShouldRefuseLockSwapAndDelete() {
        SlotMachine machine = new SlotMachine();
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "blue");
        machine.addWheel("normal", 1);
        machine.addWheel("rebel", 2);

        machine.lock(2);
        assertFalse(machine.ok());
        machine.swap(1, 2);
        assertFalse(machine.ok());
        machine.delWheel(2);
        assertFalse(machine.ok());
        assertEquals(2, machine.configuration().length);
    }

    /**
     * A lefty wheel with a wheel on its left must show the same symbol
     * as that wheel after being spun.
     */
    @Test
    public void accordingCjPmLeftyWheelShouldCopyItsLeftNeighbour() {
        SlotMachine machine = new SlotMachine();
        machine.addSymbol(1, "red");
        machine.addSymbol(2, "blue");
        machine.addSymbol(3, "green");
        machine.addWheel("normal", 1);
        machine.addWheel("lefty", 2);

        machine.spin(1, 2);
        machine.spin(2, 1);

        assertEquals(machine.configuration()[0], machine.configuration()[1]);
    }

    /**
     * simulate(n) must run without throwing any error. It needs a
     * screen, so it is skipped when the computer has none.
     */
    @Test
    public void accordingCjPmShouldRunSimulationWithoutErrors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        SlotMachineContest.simulate(3);
    }
}
