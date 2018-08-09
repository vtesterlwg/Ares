package com.playares.commons.base.promise;

import javax.annotation.Nonnull;

public interface FailablePromise<T> {
    void success(@Nonnull T t);

    void failure(@Nonnull String reason);
}
