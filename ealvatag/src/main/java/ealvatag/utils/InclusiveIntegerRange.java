/*
 * Copyright (c) 2017 Eric A. Snell
 *
 * This file is part of eAlvaTag.
 *
 * eAlvaTag is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * eAlvaTag is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with eAlvaTag.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package ealvatag.utils;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * An ranges of numbers between lower and upper, inclusive.
 * <p>
 * Created by Eric on 8/22/2015.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class InclusiveIntegerRange {
    private final int lowerBounds;
    private final int upperBounds;

    /**
     * Lower must be less than or equal to upper
     *
     * @param lowerBounds lower end of range
     * @param upperBounds upper end of range
     */
    public InclusiveIntegerRange(int lowerBounds,
                                 int upperBounds) {
        Preconditions.checkArgument(lowerBounds <= upperBounds);
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
    }

    public boolean overlaps(InclusiveIntegerRange otherRange) {
        return otherRange.contains(lowerBounds) || otherRange.contains(upperBounds) || contains(otherRange.lowerBounds) ||
                contains(otherRange.upperBounds);
    }

    public boolean contains(int value) {
        return lowerBounds <= value && value <= upperBounds;
    }

    public int clampToRange(int value) {
        if (value < lowerBounds) {
            return lowerBounds;
        }
        if (value > upperBounds) {
            return upperBounds;
        }
        return value;
    }

    public int clampToRange(float value) {
        return clampToRange(Math.round(value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InclusiveIntegerRange that = (InclusiveIntegerRange) o;
        return lowerBounds == that.lowerBounds && upperBounds == that.upperBounds;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + lowerBounds;
        hash = hash * 31 + upperBounds;
        return hash;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("lowerBounds", lowerBounds)
                .add("upperBounds", upperBounds)
                .toString();
    }

    public int getLowerBounds() {
        return lowerBounds;
    }

    public int getUpperBounds() {
        return upperBounds;
    }

    public int size() {
        return upperBounds - lowerBounds + 1;
    }
}
