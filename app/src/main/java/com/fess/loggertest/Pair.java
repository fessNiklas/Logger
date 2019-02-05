package com.fess.loggertest;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class Pair<T, U> implements Serializable {
    public final T key;
    public final U value;

    Pair(T key, U value) {
        this.key = key;
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return key + ":" + value;
    }
}