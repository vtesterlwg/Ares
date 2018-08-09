package com.playares.commons.base.promise;

public interface Promise<T> {
    void ready(T t);
}
