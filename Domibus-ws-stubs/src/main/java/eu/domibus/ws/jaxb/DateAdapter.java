package eu.domibus.ws.jaxb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:date} mapped to {@link Date}
 *
 */
public class DateAdapter extends XmlAdapter<String, LocalDate> {


    @Override
    public LocalDate unmarshal(String s) throws Exception {
        if (s == null) {
            return null;
        }
        //this is mapped to xsd:date with or without timezone
        return LocalDate.parse(s, DateTimeFormatter.ISO_DATE);
    }

    @Override
    public String marshal(LocalDate dt) throws Exception {
        if (dt == null) {
            return null;
        }
        return dt.format(DateTimeFormatter.ISO_DATE);
    }

}