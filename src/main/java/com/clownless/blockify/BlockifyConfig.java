package com.clownless.blockify;

import eu.midnightdust.lib.config.MidnightConfig;

public class BlockifyConfig extends MidnightConfig {
    @Entry public static double scale = 1;
    @Entry public static Anchor anchor = Anchor.TOP_LEFT;
    @Entry public static int posX = 0;
    @Entry public static int posY = 0;
    @Entry(width = 7, min = 7, isColor = true) public static String titleColor = "#ffffff";
    @Entry(width = 7, min = 7, isColor = true) public static String artistColor = "#ffffff";
    @Entry(width = 7, min = 7, isColor = true) public static String timeColor = "#ffffff";
    @Entry(width = 7, min = 7, isColor = true) public static String barColor = "#ffffff";
    @Entry(width = 7, min = 7, isColor = true) public static String backgroundColor = "#000000";
    @Entry(max = 255) public static int backgroundTransparency = 100;

    public enum Anchor {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}
