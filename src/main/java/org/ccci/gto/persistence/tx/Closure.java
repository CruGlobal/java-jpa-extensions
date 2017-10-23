package org.ccci.gto.persistence.tx;

@FunctionalInterface
public interface Closure<T, X extends Throwable> {
    T run() throws X;
}
