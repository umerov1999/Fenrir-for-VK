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

package ealvatag.tag;

import com.google.common.base.Optional;

import java.io.UnsupportedEncodingException;

/**
 * A no-op implementation. Especially useful for testing with {@link Optional<TagField>}
 * <p>
 * Created by Eric A. Snell on 1/21/17.
 */
public final class NullTagField implements TagField {
    public static final TagField INSTANCE = new NullTagField();

    private NullTagField() {
    }

    @Override
    public void copyContent(TagField field) {
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public byte[] getRawContent() throws UnsupportedEncodingException {
        return new byte[0];
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public void isBinary(boolean b) {
    }

    @Override
    public boolean isCommon() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
