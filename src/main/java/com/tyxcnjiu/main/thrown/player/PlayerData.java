package com.tyxcnjiu.main.thrown.player;

import net.minecraft.nbt.CompoundTag;

public class PlayerData {
    private boolean throwModeEnabled = false;
    private boolean placeBlockModeEnabled = false;

    public PlayerData() {}

    public boolean isThrowModeEnabled() {
        return throwModeEnabled;
    }

    public void setThrowModeEnabled(boolean enabled) {
        this.throwModeEnabled = enabled;
    }

    public boolean isPlaceBlockModeEnabled() {
        return placeBlockModeEnabled;
    }

    public void setPlaceBlockModeEnabled(boolean enabled) {
        this.placeBlockModeEnabled = enabled;
    }

    public void resetState() {
        throwModeEnabled = false;
        placeBlockModeEnabled = false;
    }

    public void saveNBTData(CompoundTag compound) {
        compound.putBoolean("throwModeEnabled", throwModeEnabled);
        compound.putBoolean("placeBlockModeEnabled", placeBlockModeEnabled);
    }

    public void loadNBTData(CompoundTag compound) {
        throwModeEnabled = compound.getBoolean("throwModeEnabled");
        placeBlockModeEnabled = compound.getBoolean("placeBlockModeEnabled");
    }
}
