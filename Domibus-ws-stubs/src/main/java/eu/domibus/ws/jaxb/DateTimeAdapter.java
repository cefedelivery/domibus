package eu.domibus.ws.jaxb;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:dateTime} mapped to {@link DateTime}
 */
public class DateTimeAdapter extends XmlAdapter<String, LocalDateTime> {


    @Override
    public LocalDateTime unmarshal(String s) throws Exception {
        if (s == null) {
            return null;
        }
        return LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public String marshal(LocalDateTime dt) throws Exception {
        if (dt == null) {
            return null;
        }
        return dt.format(DateTimeFormatter.ISO_DATE_TIME);
    }

}