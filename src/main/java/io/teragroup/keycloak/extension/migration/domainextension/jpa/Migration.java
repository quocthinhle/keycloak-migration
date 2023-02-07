// SPDX-License-Identifier: Apache-2.0

package io.teragroup.keycloak.extension.migration.domainextension.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@Table(name = "MIGRATION")
@NamedQueries({
        @NamedQuery(name = "listOne", query = "select m from Migration m limit 1"),
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
