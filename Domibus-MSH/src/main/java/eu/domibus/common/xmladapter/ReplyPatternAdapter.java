
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
