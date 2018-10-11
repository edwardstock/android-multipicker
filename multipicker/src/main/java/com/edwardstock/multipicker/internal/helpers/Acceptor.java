package com.edwardstock.multipicker.internal.helpers;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public interface Acceptor<T> {
    void call(T t);
}
