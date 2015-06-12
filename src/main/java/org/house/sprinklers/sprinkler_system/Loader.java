package org.house.sprinklers.sprinkler_system;


public interface Loader<T, O> {
    O load(T t) throws Exception;
}
