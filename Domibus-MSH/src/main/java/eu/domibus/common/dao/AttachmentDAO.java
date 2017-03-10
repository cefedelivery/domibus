/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */


package eu.domibus.common.dao;

import eu.domibus.ebms3.common.model.PartInfo;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * DAO to access binary payload data of a {@link PartInfo} object
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Repository
public class AttachmentDAO {

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager em;

    public byte[] loadBinaryData(final int entityId) {
        final Query q = this.em.createNamedQuery("PartInfo.loadBinaryData");
        q.setParameter("ENTITY_ID", entityId);
        return (byte[]) q.getSingleResult();
    }
}
