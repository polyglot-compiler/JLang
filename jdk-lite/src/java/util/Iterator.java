//Copyright (C) 2018 Cornell University

package java.util;

public interface Iterator<E> {

    boolean hasNext();

    E next();

    void remove();
}
