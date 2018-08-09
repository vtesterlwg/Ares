package com.playares.commons.base.promise;

import javax.annotation.Nonnull;

public interface SimplePromise {
    void success();

    void failure(@Nonnull String reason);
}
