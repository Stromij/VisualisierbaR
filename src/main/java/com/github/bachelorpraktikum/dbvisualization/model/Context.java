package com.github.bachelorpraktikum.dbvisualization.model;

import javax.annotation.concurrent.Immutable;

/**
 * Provides a context for factories of classes in this package.<br>Classes in this package always
 * ensure the uniqueness of names for an instance of Context.<br>If an instance of this class is no
 * longer referenced in any client code, all associated data will be garbage collected.
 */
@Immutable
public final class Context {
    public Context() {
    }
}
