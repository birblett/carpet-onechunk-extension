package com.birblett.util;

public interface VaultCooldownHolder {
    long getCustomCooldownEnd();
    void setCustomCooldownEnd(long tick);
}
