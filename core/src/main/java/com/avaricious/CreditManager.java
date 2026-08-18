package com.avaricious;

import com.avaricious.utility.Observable;

public class CreditManager extends Observable<Integer> {

    private static CreditManager instance;

    public static CreditManager I() {
        return instance == null ? (instance = new CreditManager()) : instance;
    }

    private CreditManager() {
        setCredits(5);
    }

    private int credits;

    public void gain(int amount) {
        setCredits(credits + amount);
    }

    public void pay(int amount) {
        if (!DevTools.unlimitedMoney())
            setCredits(credits - amount);
    }

    public void roundEnd() {
        gain((int) (3 + Math.min((double) (credits / 5), 5)));
    }

    public int getCredits() {
        return credits;
    }

    public void pulse() {
        setCredits(getCredits());
    }

    public void setCredits(int newValue) {
        credits = newValue;
        notifyChanged(newValue);
    }

    public boolean enoughCredit(int value) {
        return credits - value >= 0;
    }

    @Override
    protected Integer snapshot() {
        return credits;
    }
}
