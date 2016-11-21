package com.github.bachelorpraktikum.dbvisualization.model;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@ParametersAreNonnullByDefault
public final class Coordinates {
    private final int x;
    private final int y;

    public Coordinates(int x, int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Coordinate can't be negative");
        }
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Coordinates that = (Coordinates) obj;

        if (x != that.x) return false;
        return y == that.y;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "Coordinates{"
                + "x=" + x
                + ", y=" + y
                + '}';
    }
}
