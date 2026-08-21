// com.avaricious.components.slot.pattern.PatternMatch

package com.avaricious.components.slot.pattern;

import com.avaricious.components.slot.Body;
import com.avaricious.components.slot.SlotMachine;
import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.Seq;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class PatternMatch {

    private final Symbol symbol;
    private final int length;
    private final List<Vector2> positions;
    private final UnlockablePattern pattern;

    public PatternMatch(Symbol symbol, int length, List<Vector2> positions) {
        this(symbol, length, positions, null);
    }

    public PatternMatch(
        Symbol symbol,
        int length,
        List<Vector2> positions,
        UnlockablePattern pattern
    ) {
        this.symbol = symbol;
        this.length = length;
        this.positions = positions;
        this.pattern = pattern;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public int getLength() {
        return length;
    }

    public List<Vector2> getPositions() {
        return positions;
    }

    public UnlockablePattern getPattern() {
        return pattern;
    }

    public List<Body> getSlots() {
        Body[][] slotMachineGrid = SlotMachine.I().getGrid();
        return Seq.of(positions)
            .map(pos -> slotMachineGrid[(int) pos.x][(int) pos.y])
            .toList();
    }
}
