package com.avaricious.components.slot.rework;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.SeededRandomizer;
import com.avaricious.utility.Seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpinResultGenerator {

    public SpinResult generate(int cols, int rows) {
        Symbol[][] result = new Symbol[cols][rows];

        List<Symbol> pool = buildWeightedPool();

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                result[c][r] = pool.get(
                    SeededRandomizer.nextInt(0, pool.size() - 1)
                );
            }
        }

        return new SpinResult(result);
    }

    private List<Symbol> buildWeightedPool() {
        List<Symbol> pool = new ArrayList<>();

        Seq.of(Arrays.asList(Symbol.values()))
            .forEach(symbol -> {
                for (int i = 0; i < symbol.poolCount(); i++) {
                    pool.add(symbol);
                }
            });

        return pool;
    }
}
