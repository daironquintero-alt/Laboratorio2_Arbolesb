package com.udea.co.demo;

public class InsertResult {
    public final boolean inserted;
    public final boolean hadOverflow;

    InsertResult(boolean inserted, boolean hadOverflow) {
        this.inserted = inserted;
        this.hadOverflow = hadOverflow;
    }
}
