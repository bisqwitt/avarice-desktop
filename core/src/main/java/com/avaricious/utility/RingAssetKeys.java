package com.avaricious.utility;

import java.util.concurrent.ThreadLocalRandom;

public enum RingAssetKeys {
    RING_4(AssetKey.RING_4, AssetKey.RING_4_SHADOW, AssetKey.RING_24_WHITE),
    RING_12(AssetKey.RING_12, AssetKey.RING_12_SHADOW, AssetKey.RING_12_WHITE),
    RING_19(AssetKey.RING_19, AssetKey.RING_19_SHADOW, AssetKey.RING_19_WHITE),
    RING_24(AssetKey.RING_24, AssetKey.RING_24_SHADOW, AssetKey.RING_24_WHITE),
    RING_25(AssetKey.RING_25, AssetKey.RING_25_SHADOW, AssetKey.RING_25_WHITE),
    RING_31(AssetKey.RING_31, AssetKey.RING_31_SHADOW, AssetKey.RING_31_WHITE),
    RING_32(AssetKey.RING_32, AssetKey.RING_32_SHADOW, AssetKey.RING_32_WHITE),
    RING_43(AssetKey.RING_43, AssetKey.RING_43_SHADOW, AssetKey.RING_43_WHITE),
    RING_44(AssetKey.RING_44, AssetKey.RING_44_SHADOW, AssetKey.RING_44_WHITE),
    RING_58(AssetKey.RING_58, AssetKey.RING_58_SHADOW, AssetKey.RING_58_WHITE),
    RING_77(AssetKey.RING_77, AssetKey.RING_77_SHADOW, AssetKey.RING_77_WHITE),
    RING_80(AssetKey.RING_80, AssetKey.RING_80_SHADOW, AssetKey.RING_80_WHITE),
    RING_93(AssetKey.RING_93, AssetKey.RING_93_SHADOW, AssetKey.RING_93_WHITE),
    RING_95(AssetKey.RING_95, AssetKey.RING_95_SHADOW, AssetKey.RING_95_WHITE);

    private final AssetKey assetKey;
    private final AssetKey shadowKey;
    private final AssetKey whiteKey;

    RingAssetKeys(AssetKey assetKey, AssetKey shadowKey, AssetKey whiteKey) {
        this.assetKey = assetKey;
        this.shadowKey = shadowKey;
        this.whiteKey = whiteKey;
    }

    public AssetKey getTextureKey() {
        return assetKey;
    }

    public AssetKey getShadowKey() {
        return shadowKey;
    }

    public AssetKey getWhiteKey() {
        return whiteKey;
    }

    public static RingAssetKeys random() {
        return values()[ThreadLocalRandom.current().nextInt(values().length)];
    }

    public static RingAssetKeys getRingKeyByDefaultKey(AssetKey assetKey) {
        for (RingAssetKeys ringKey : values()) {
            if (ringKey.assetKey == assetKey) return ringKey;
        }
        return null;
    }
}
