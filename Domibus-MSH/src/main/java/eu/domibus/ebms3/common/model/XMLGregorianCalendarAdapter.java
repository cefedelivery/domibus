package eu.domibus.ebms3.common.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
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
