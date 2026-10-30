package com.birblett.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TextUtils {

    public static MutableComponent formattable(String s) {
        return Component.literal(s);
    }

    public static MutableComponent translatable(String s) {
        return Component.translatable(s);
    }

}
