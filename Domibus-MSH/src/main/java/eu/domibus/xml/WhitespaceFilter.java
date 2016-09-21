package eu.domibus.xml;

import javax.xml.stream.EventFilter;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

class WhitespaceFilter implements EventFilter {
    
    @Override
    public boolean accept(XMLEvent event) {
        return !(event.isCharacters() && ((Characters) event).isWhiteSpace());
    }
}
