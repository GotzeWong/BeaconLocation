package com.kyvlabs.beaconadvertiser.views;

import android.text.InputFilter;
import android.text.Spanned;

//min/max edit text validator
public class InputFilterMinMax implements InputFilter {

    private int min, max;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned destination, int destinationStart, int destinationEnd) {
        try {
            int input = Integer.parseInt(destination.toString() + source.toString());
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException ignored) {
        }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}