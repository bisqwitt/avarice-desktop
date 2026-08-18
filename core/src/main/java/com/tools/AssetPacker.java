package com.tools;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class AssetPacker {

    public static void main(String[] args) throws Exception {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth = 4096;
        settings.maxHeight = 4096;
        settings.pot = false;
        settings.square = false;

        TexturePacker.process(settings, "assets-raw", "assets", "atlases");
    }

}
