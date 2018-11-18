package com.riotmc.commons.bukkit.timer;

import com.riotmc.commons.base.util.Time;
import lombok.Getter;
import lombok.Setter;

public abstract class Timer {
    @Setter
    public long expire;

    @Getter @Setter
    public long frozenTime;

    @Getter
    public boolean frozen;

    public Timer(int seconds) {
        this.expire = Time.now() + (seconds * 1000);
        this.frozenTime = 0L;
        this.frozen = false;
    }

    public Timer(long milliseconds) {
        this.expire = Time.now() + milliseconds;
        this.frozenTime = 0L;
        this.frozen = false;
    }

    /**
     * @return Expire milliseconds
     */
    public long getExpire() {
        return frozen ? frozenTime + Time.now() : expire;
    }

    /**
     * @return Remaining milliseconds
     */
    public long getRemaining() {
        return getExpire() - Time.now();
    }

    /**
     * @return True if expired
     */
    public boolean isExpired() {
        return getExpire() <= Time.now();
    }

    /**
     * Freezes the timer
     */
    public void freeze() {
        frozen = true;
        frozenTime = getRemaining();
    }

    /**
     * Unfreezes the timer
     */
    public void unfreeze() {
        expire = getExpire();
        frozen = false;
        frozenTime = 0L;
    }

    /**
     * Called when this timer is finished
     */
    public abstract void onFinish();
}