package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

/**
 * An {@link ObservableList} implementation that is composed of multiple observable lists each
 * containing an {@link Event} subtype.
 */
@ParametersAreNonnullByDefault
class CompositeObservableList<T> extends ObservableListBase<T> {
    private final List<ObservableList<? extends T>> lists;

    /**
     * Creates a new instance. A defensive copy of the given list is created, so changes to the list
     * after calling this constructor won't be represented in this object.
     *
     * @param lists the lists this list is composed of
     * @throws NullPointerException if lists is null
     */
    CompositeObservableList(List<ObservableList<? extends T>> lists) {
        this.lists = new ArrayList<>(Objects.requireNonNull(lists));
        registerListeners();
    }

    /**
     * Registers change listeners for all lists this list is composed of.
     */
    private void registerListeners() {
        for (ObservableList<? extends T> list : lists) {
            list.addListener(this::fireChange);
        }
    }

    @Override
    public T get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index is negative");
        }

        for (ObservableList<? extends T> list : lists) {
            if (list.size() <= index) {
                index -= list.size();
            } else {
                return list.get(index);
            }
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        return lists.stream().mapToInt(List::size).sum();
    }
}
