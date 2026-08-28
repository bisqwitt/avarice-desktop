package com.avaricious.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum AssetAnimationKey {

    CHEST_OPEN(
        AssetKey.CHEST_CLOSED,
        AssetKey.CHEST_OPENING1,
        AssetKey.CHEST_OPENING2,
        AssetKey.CHEST_OPENING3,
        AssetKey.CHEST_OPENING4,
        AssetKey.CHEST_OPENING5,
        AssetKey.CHEST_OPENING6
    ),

    DUMPSTER_CLOSE(
        AssetKey.DUMPSTER_OPENED,
        AssetKey.DUMPSTER_1,
        AssetKey.DUMPSTER_2,
        AssetKey.DUMPSTER_3,
        AssetKey.DUMPSTER_4,
        AssetKey.DUMPSTER_CLOSED
    ),
    DUMPSTER_OPEN(
        AssetKey.DUMPSTER_CLOSED,
        AssetKey.DUMPSTER_4,
        AssetKey.DUMPSTER_3,
        AssetKey.DUMPSTER_2,
        AssetKey.DUMPSTER_1,
        AssetKey.DUMPSTER_OPENED
    );

    private final List<AssetKey> frameAssetKeys;

    AssetAnimationKey(AssetKey... keys) {
        frameAssetKeys = new ArrayList<>(Arrays.asList(keys));
    }

    public List<AssetKey> getFrameAssetKeys() {
        return frameAssetKeys;
    }
}
