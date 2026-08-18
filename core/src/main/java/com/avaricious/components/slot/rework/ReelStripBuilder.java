package com.avaricious.components.slot.rework;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.SeededRandomizer;
import com.avaricious.utility.Seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReelStripBuilder {

    public List<Symbol> buildBaseStrip() {
        List<Symbol> baseStrip = new ArrayList<>();

        Seq.of(Arrays.asList(Symbol.values()))
            .forEach(symbol -> {
                for (int i = 0; i < symbol.poolCount() / 2; i++) {
                    baseStrip.add(symbol);
                }
            });

        return baseStrip;
    }

    public List<Symbol> buildShuffledStrip(List<Symbol> baseStrip) {
        List<Symbol> strip = new ArrayList<>(baseStrip);
        Collections.shuffle(strip, SeededRandomizer.get());
        return strip;
    }
}
