/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.lang.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

/**
 * @author Nikolay Krasko
 */
public final class ImportPath {
    private final @NotNull FqName fqName;
    private final @Nullable Name alias;
    private final boolean isAllUnder;

    public ImportPath(@NotNull FqName fqName, boolean isAllUnder) {
        this(fqName, isAllUnder, null);
    }

    public ImportPath(@NotNull FqName fqName, boolean isAllUnder, @Nullable Name alias) {
        this.fqName = fqName;
        this.isAllUnder = isAllUnder;
        this.alias = alias;
    }

    public ImportPath(@NotNull String pathStr) {
        if (pathStr.endsWith(".*")) {
            this.isAllUnder = true;
            this.fqName = new FqName(pathStr.substring(0, pathStr.length() - 2));
        }
        else {
            this.isAllUnder = false;
            this.fqName = new FqName(pathStr);
        }

        alias = null;
    }

    public String getPathStr() {
        return fqName.getFqName() + (isAllUnder ? ".*" : "");
    }

    @Override
    public String toString() {
        return getPathStr();
    }

    @NotNull
    public FqName fqnPart() {
        return fqName;
    }

    @Nullable
    public Name getAlias() {
        return alias;
    }

    public boolean hasAlias() {
        return alias != null;
    }

    public boolean isAllUnder() {
        return isAllUnder;
    }

    @Nullable
    public Name getImportedName() {
        if (!isAllUnder()) {
            return alias != null ? alias : fqName.shortName();
        }

        return null;
    }

    @Override
    public int hashCode() {
        int result = 31 * fqName.hashCode() + (isAllUnder ? 1 : 0);
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ImportPath)) return false;

        ImportPath other = (ImportPath) obj;

        return fqName.equals(other.fqName) && (isAllUnder == other.isAllUnder) &&
               ((alias == null && other.alias == null) || (alias != null && alias.equals(other.alias)));
    }
}
