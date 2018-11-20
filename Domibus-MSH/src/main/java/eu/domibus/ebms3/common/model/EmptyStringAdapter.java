package eu.domibus.ebms3.common.model;

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;

/**
 * This adapter removes leading and trailing whitespaces, then truncates any
 * sequence of tab, CR, LF, and SP by a single whitespace character ' ',
 * then makes sure an empty string is transformed to a single space string.
 *
 * @author Ion Perpegel
 * @since 4.0.1
 */
public class EmptyStringAdapter extends CollapsedStringAdapter {
    @Override
    public String unmarshal(String v) {
        v = super.unmarshal(v);
        return StringUtils.EMPTY.equals(v) ? StringUtils.SPACE : v;
    }

    @Override
    public String marshal(String v) {
        v = super.marshal(v);
        return StringUtils.SPACE.equals(v) ? StringUtils.EMPTY : v;
    }
}