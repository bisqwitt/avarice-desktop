package com.avaricious.utility;

import java.util.ArrayList;

public final class IntSeq {
    private final Iterable<Integer> source;

    public IntSeq(Iterable<Integer> source) {
        this.source = source;
    }

    public int maxOrDefault(int defaultValue) {
        boolean hasValue = false;
        int max = defaultValue;

        for (Integer item : source) {
            if (item == null) {
                continue;
            }

            if (!hasValue || item > max) {
                max = item;
                hasValue = true;
            }
        }

        return max;
    }

    public ArrayList<Integer> toList() {
        ArrayList<Integer> out = new ArrayList<Integer>();

        for (Integer item : source) {
            out.add(item);
        }

        return out;
    }
}
