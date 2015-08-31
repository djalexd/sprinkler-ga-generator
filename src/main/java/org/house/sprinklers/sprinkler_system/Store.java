package org.house.sprinklers.sprinkler_system;

public interface Store<T, O> {
    O save(T t);
}
