package com.playares.commons.bukkit.timer;

import com.playares.commons.base.util.Time;
import lombok.Getter;
import lombok.Setter;

public abstract class Timer {
    private long expire;

    @Getter @Setter
    public long frozenTime;

    @Getter
    public boolean frozen;

    public long getExpire() {
        return frozen ? frozenTime + Time.now() : expire;
    }

    public long getRemaining() {
        return getExpire() - Time.now();
    }

    public boolean isExpired() {
        return getExpire() <= Time.now();
    }

    public void freeze() {
        frozen = true;
        frozenTime = getRemaining();
    }

    public void unfreeze() {
        expire = getExpire();
        frozen = false;
        frozenTime = 0L;
    }

    public abstract void onFinish();
}
