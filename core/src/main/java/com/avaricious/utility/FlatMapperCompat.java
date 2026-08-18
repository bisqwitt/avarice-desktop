package com.avaricious.utility;

public interface FlatMapperCompat<T, R> {
    Iterable<R> apply(T value);
}
