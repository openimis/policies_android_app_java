package org.openimis.imispolicies.network.util;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Mapper<T, U> {

    @NonNull
    public static <T,U> List<U> map(@NonNull List<T> list, @NonNull Transformer<T, U> transformer) {
        return new Mapper<>(transformer).map(list);
    }

    @NonNull
    private final Transformer<T,U> transformer;

    public Mapper(@NonNull Transformer<T,U> transformer) {
        this.transformer = transformer;
    }

    @NonNull
    public List<U> map(@NonNull List<T> list) {
        List<U> newList = new ArrayList<>();
        for (T item: list) {
            newList.add(transformer.transform(item));
        }
        return newList;
    }

    public interface Transformer<T,U> {
        @NonNull U transform(T object);
    }
}
