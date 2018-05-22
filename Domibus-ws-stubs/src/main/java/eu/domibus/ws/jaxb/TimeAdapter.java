package eu.domibus.ws.jaxb;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:time} mapped to {@link LocalTime}

 */
public class TimeAdapter extends XmlAdapter<String, LocalTime> {



    @Override
    public LocalTime unmarshal(String s) throws Exception {
        if (s == null) {
            return null;
        }
        return LocalTime.parse(s, DateTimeFormatter.ISO_TIME);
    }

    @Override
    public String marshal(LocalTime lt) throws Exception {
        if (lt == null) {
            return null;
        }
        return lt.format(DateTimeFormatter.ISO_TIME);
    }

}