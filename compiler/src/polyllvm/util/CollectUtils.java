package polyllvm.util;

import java.lang.reflect.Array;
import java.util.List;

public final class CollectUtils {

    static public <T> T[] toArray(List<? extends T> l, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T[] res = (T[]) Array.newInstance(clazz, l.size());
        int idx = 0;
        for (T t : l)
            res[idx++] = t;
        return res;
    }

    static public <T> T[] toArray(T head, List<? extends T> l, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T[] res = (T[]) Array.newInstance(clazz, 1 + l.size());
        int idx = 0;
        res[idx++] = head;
        for (T t : l)
            res[idx++] = t;
        return res;
    }

    static public <T> T[] toArray(T t1, T t2, List<? extends T> l,
            Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T[] res = (T[]) Array.newInstance(clazz, 2 + l.size());
        int idx = 0;
        res[idx++] = t1;
        res[idx++] = t2;
        for (T t : l)
            res[idx++] = t;
        return res;
    }

}
