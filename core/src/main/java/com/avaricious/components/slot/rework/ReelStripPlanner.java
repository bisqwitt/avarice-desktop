package com.avaricious.components.slot.rework;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.SeededRandomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReelStripPlanner {

    public PlannedReel createPlannedReel(
        List<Symbol> baseStrip,
        SpinResult result,
        int col,
        int rows
    ) {
        List<Symbol> strip = new ArrayList<>(baseStrip);

        Collections.shuffle(strip, SeededRandomizer.get());

        int targetIndex = SeededRandomizer.nextInt(
            rows + 2,
            strip.size() - rows - 2
        );

        for (int row = 0; row < rows; row++) {
            strip.set(targetIndex - row, result.get(col, row));
        }

        return new PlannedReel(strip, targetIndex);
    }

    public static class PlannedReel {
        public final List<Symbol> strip;
        public final int targetIndex;

        public PlannedReel(List<Symbol> strip, int targetIndex) {
            this.strip = strip;
            this.targetIndex = targetIndex;
        }
    }
}
