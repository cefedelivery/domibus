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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * XMLAdapter to transform XMLGregorianCalendar to Date with timezone=UTC
 *
 * @author Christian Koch, Stefan Mueller
 * @version 1.0
 * @since 3.0
 */
public class XMLGregorianCalendarAdapter extends XmlAdapter<XMLGregorianCalendar, Date> {
    private final DatatypeFactory datatypeFactory;

    public XMLGregorianCalendarAdapter() throws DatatypeConfigurationException {
        this.datatypeFactory = DatatypeFactory.newInstance();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public Date unmarshal(final XMLGregorianCalendar xmlGregorianCalendar) throws Exception {
        final GregorianCalendar utcGregorianCalendar = xmlGregorianCalendar.toGregorianCalendar();
        utcGregorianCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return utcGregorianCalendar.getTime();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public XMLGregorianCalendar marshal(final Date date) throws Exception {
        final GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        return this.datatypeFactory.newXMLGregorianCalendar(calendar);
    }
}
