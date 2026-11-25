package com.birblett.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TextUtils {

    private static String surroundWith(String s, String surround) {
        return surround + s + surround;
    }

    public static String deformatted(MutableComponent message) {
        StringBuilder s = new StringBuilder();
        for (Component c : message.getSiblings()) {
            String tmp = c.getString();
            Style style = c.getStyle();
            if (style.isItalic()) {
                tmp = surroundWith(tmp, "*");
            }
            if (style.isBold()) {
                tmp = surroundWith(tmp, "**");
            }
            if (style.isUnderlined()) {
                tmp = surroundWith(tmp, "__");
            }
            if (style.isStrikethrough()) {
                tmp = surroundWith(tmp, "~~");
            }
            s.append(tmp);
        }
        return s.toString();
    }

    public static MutableComponent formattable(String s) {
        return Component.literal(s);
    }

    public static MutableComponent translatable(String s) {
        return Component.translatable(s);
    }

}
