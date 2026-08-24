package com.avaricious;

public class DevTools {

    private static final boolean active = true;
    private static boolean freeShopPurchases = false;

    public static boolean audioMuted() {
        return active && false;
    }

    public static boolean enableProfiler() {
        return active && false;
    }

    public static boolean allCardsInDeck() {
        return active && false;
    }

    public static boolean unlimitedMoney() {
        return active && false;
    }

    public static boolean freeShopPurchases() {
        return active && freeShopPurchases;
    }

    public static boolean toggleFreeShopPurchases() {
        if (!active) return false;
        freeShopPurchases = !freeShopPurchases;
        return freeShopPurchases;
    }

    public static boolean showMouseLocation() {
        return active && false;
    }

}
