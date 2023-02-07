// SPDX-License-Identifier: Apache-2.0

package io.teragroup.keycloak.extension.migration.domainextension;

import io.teragroup.keycloak.extension.migration.domainextension.jpa.Migration;

public class MigrationRepresentation {

    private int version;
    private boolean dirty;

    public MigrationRepresentation() {
    }

    public MigrationRepresentation(Migration migration) {
        version = migration.getVersion();
        dirty = migration.getDirty();
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public boolean getDirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

}
