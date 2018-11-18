package com.riotmc.commons.base.promise;

import javax.annotation.Nonnull;

public interface FailablePromise<T> {
    /**
     * Success with a prepared result
     * @param t Prepared result
     */
    void success(@Nonnull T t);

    /**
     * Failure
     * @param reason Failure response
     */
    void failure(@Nonnull String reason);
}
