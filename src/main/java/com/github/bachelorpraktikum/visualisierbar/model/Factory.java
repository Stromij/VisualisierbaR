package com.github.bachelorpraktikum.visualisierbar.model;

import java.util.Collection;
import javax.annotation.Nonnull;

/**
 * <p>Manages instances of a class implementing GraphObject. Does not allow two objects to have the
 * same name.</p>
 *
 * <p>Implementing classes should also provide a create(name, ...) method returning an instance of
 * type T.</p>
 *
 * @param <T> the type of GraphObject which's instances this EdgeFactory manages
 */
public interface Factory<T extends GraphObject<?>> {

    /**
     * Gets the instance with the given unique name.
     *
     * @param name the instance's name
     * @return the instance with this name
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if there is no object associated with the name
     */
    @Nonnull
    T get(@Nonnull String name);

    /**
     * Gets all instances in this {@link Context}.
     *
     * @return all instances
     */
    @Nonnull
    Collection<T> getAll();

    /**
     * Checks whether the given instance of T was created by this factory.
     *
     * @param t the instance to check affiliation for
     * @return whether the given object was created by this factory
     */
    boolean checkAffiliated(@Nonnull T t);

    /**
     * Check whether a name is already taken
     * @param name the Name to check
     * @return  true if name is already taken, false otherwise
     */
    boolean NameExists(@Nonnull String name);
}
