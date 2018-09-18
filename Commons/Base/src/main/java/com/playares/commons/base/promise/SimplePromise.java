package com.playares.commons.base.promise;

import javax.annotation.Nonnull;

public interface SimplePromise {
    /**
     * Success
     */
    void success();

    /**
     * Failure
     * @param reason Error response
     */
    void failure(@Nonnull String reason);
}
