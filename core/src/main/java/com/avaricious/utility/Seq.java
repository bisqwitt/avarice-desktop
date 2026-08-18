package com.avaricious.utility;

import java.util.ArrayList;
import java.util.Arrays;

public final class Seq<T> {
    private final Iterable<T> source;

    private Seq(Iterable<T> source) {
        this.source = source;
    }

    public static <T> Seq<T> of(Iterable<T> source) {
        return new Seq<T>(source);
    }

    public static <T> Seq<T> of(final T[] array) {
        return new Seq<T>(Arrays.asList(array));
    }

    public static <T> Seq<T> of(final T[][] array) {
        ArrayList<T> out = new ArrayList<T>();

        for (T[] ts : array) {
            out.addAll(Arrays.asList(ts));
        }

        return new Seq<T>(out);
    }

    public void forEach(ActionCompat<T> action) {
        for (T item : source) {
            action.accept(item);
        }
    }

    public Seq<T> filter(PredicateCompat<T> predicate) {
        ArrayList<T> out = new ArrayList<T>();
        for (T item : source) {
            if (predicate.test(item)) {
                out.add(item);
            }
        }
        return new Seq<T>(out);
    }

    public <R> Seq<R> map(FunctionCompat<T, R> mapper) {
        ArrayList<R> out = new ArrayList<R>();
        for (T item : source) {
            out.add(mapper.apply(item));
        }
        return new Seq<R>(out);
    }

    public <R> Seq<R> flatMap(FlatMapperCompat<T, R> mapper) {
        ArrayList<R> out = new ArrayList<R>();

        for (T item : source) {
            Iterable<R> mapped = mapper.apply(item);

            if (mapped == null) {
                continue;
            }

            for (R mappedItem : mapped) {
                out.add(mappedItem);
            }
        }

        return new Seq<R>(out);
    }

    public ArrayList<T> toList() {
        ArrayList<T> out = new ArrayList<T>();
        for (T item : source) {
            out.add(item);
        }
        return out;
    }

    public T findFirstOrNull() {
        for (T item : source) {
            return item;
        }

        return null;
    }

    public boolean anyMatch(PredicateCompat<T> predicate) {
        for (T item : source) {
            if (predicate.test(item)) {
                return true;
            }
        }

        return false;
    }

    public T findAnyOrNull() {
        for (T item : source) {
            return item;
        }

        return null;
    }

    public IntSeq mapToInt(IntMapperCompat<T> mapper) {
        ArrayList<Integer> out = new ArrayList<Integer>();

        for (T item : source) {
            out.add(mapper.applyAsInt(item));
        }

        return new IntSeq(out);
    }
}
