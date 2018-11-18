package com.riotmc.commons.base.promise;

public interface Promise<T> {
    /**
     * Prepared result
     * @param t Type
     */
    void ready(T t);
}
