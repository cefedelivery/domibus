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

package eu.domibus.common.xmladapter;

import eu.domibus.common.model.configuration.ReplyPattern;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * TODO: add class description
 */
public class ReplyPatternAdapter extends XmlAdapter<String, ReplyPattern> {
    @Override
    public ReplyPattern unmarshal(final String v) throws Exception {
        return ReplyPattern.valueOf(v.toUpperCase());
    }

    @Override
    public String marshal(final ReplyPattern v) throws Exception {
        return v.name().toLowerCase();
    }
}
