// SPDX-License-Identifier: Apache-2.0

package io.teragroup.keycloak.extension.migration.domainextension.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

@Entity
@Table(name = "MIGRATION")
@NamedQueries({
        @NamedQuery(name = "list", query = "select m from Migration m"),
})
public class Migration {

    @Id
    @Column(name = "VERSION")
    private int version;

    @Column(name = "DIRTY")
    private boolean dirty;

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
