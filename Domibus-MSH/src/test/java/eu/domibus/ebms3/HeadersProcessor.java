package eu.domibus.ebms3;

import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.staxutils.StaxUtils.StreamToDOMContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class HeadersProcessor {
        private static XMLEventFactory eventFactory;
        private final String ns;
        private final String header;
        private final String body;
        private final String envelope;
        private final List<XMLEvent> events = new ArrayList<>(8);
        private List<XMLEvent> envEvents;
        private List<XMLEvent> bodyEvents;
        private StreamToDOMContext context;
        private Document doc;
        private Node parent;
        private QName lastStartElementQName;
        private String envelopePrefix;
        private String bodyPrefix;

        static {
            try {
                eventFactory = XMLEventFactory.newInstance();
            } catch (Throwable t) {
                //explicity create woodstox event factory as last try
                eventFactory = StaxUtils.createWoodstoxEventFactory();
            }
        }

        HeadersProcessor(SoapVersion version) {
            this.header = version.getHeader().getLocalPart();
            this.ns = version.getEnvelope().getNamespaceURI();
            this.envelope = version.getEnvelope().getLocalPart();
            this.body = version.getBody().getLocalPart();
        }

        public Document process(XMLStreamReader reader) throws XMLStreamException {
            // number of elements read in
            int read = 0;
            int event = reader.getEventType();
            while (reader.hasNext()) {
                switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    read++;
                    addEvent(eventFactory.createStartElement(new QName(reader.getNamespaceURI(), reader
                                                            .getLocalName(), reader.getPrefix()), null, null));
                    for (int i = 0; i < reader.getNamespaceCount(); i++) {
                        addEvent(eventFactory.createNamespace(reader.getNamespacePrefix(i),
                                                         reader.getNamespaceURI(i)));
                    }
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        addEvent(eventFactory.createAttribute(reader.getAttributePrefix(i),
                                                         reader.getAttributeNamespace(i),
                                                         reader.getAttributeLocalName(i),
                                                         reader.getAttributeValue(i)));
                    }
                    if (doc != null) {
                        //go on parsing the stream directly till the end and stop generating events
                        StaxUtils.readDocElements(doc, parent, reader, context);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (read > 0) {
                        addEvent(eventFactory.createEndElement(new QName(reader.getNamespaceURI(), reader
                                                              .getLocalName(), reader.getPrefix()), null));
                    }
                    read--;
                    break;
                case XMLStreamConstants.CHARACTERS:
                    String s = reader.getText();
                    if (s != null) {
                        addEvent(eventFactory.createCharacters(s));
                    }
                    break;
                case XMLStreamConstants.COMMENT:
                    addEvent(eventFactory.createComment(reader.getText()));
                    break;
                case XMLStreamConstants.CDATA:
                    addEvent(eventFactory.createCData(reader.getText()));
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                case XMLStreamConstants.END_DOCUMENT:
                case XMLStreamConstants.ATTRIBUTE:
                case XMLStreamConstants.NAMESPACE:
                    break;
                default:
                    break;
                }
                event = reader.next();
            }

            return doc;
        }

        private void addEvent(XMLEvent event) {
            if (event.isStartElement()) {
                lastStartElementQName = event.asStartElement().getName();
                if (header.equals(lastStartElementQName.getLocalPart())
                    && ns.equals(lastStartElementQName.getNamespaceURI())) {
                    // process all events recorded so far
                    context = new StreamToDOMContext(true, false, false);
                    doc = DOMUtils.createDocument();
                    parent = doc;
                    try {
                        for (XMLEvent ev : events) {
                            parent = StaxUtils.readDocElement(doc, parent, ev, context);
                        }
                    } catch (XMLStreamException e) {
                        throw new Fault(e);
                    }
                } else {
                    if (ns.equals(lastStartElementQName.getNamespaceURI())) {
                        if (body.equals(lastStartElementQName.getLocalPart())) {
                            bodyPrefix = lastStartElementQName.getPrefix();
                        } else if (envelope.equals(lastStartElementQName.getLocalPart())) {
                            envelopePrefix = lastStartElementQName.getPrefix();
                        }
                    }
                    events.add(event);
                }
            } else {
                if (event.isNamespace() || event.isAttribute()) {
                    final String lastEl = lastStartElementQName.getLocalPart();
                    if (body.equals(lastEl) && ns.equals(lastStartElementQName.getNamespaceURI())) {
                        if (bodyEvents == null) {
                            bodyEvents = new ArrayList<>();
                        }
                        bodyEvents.add(event);
                    } else if (envelope.equals(lastEl) && ns.equals(lastStartElementQName.getNamespaceURI())) {
                        if (envEvents == null) {
                            envEvents = new ArrayList<>();
                        }
                        envEvents.add(event);
                    }
                }
                events.add(event);
            }
        }

        public List<XMLEvent> getBodyAttributeAndNamespaceEvents() {
            if (bodyEvents == null) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(bodyEvents);
        }

        public List<XMLEvent> getEnvAttributeAndNamespaceEvents() {
            if (envEvents == null) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(envEvents);
        }

        public String getEnvelopePrefix() {
            return envelopePrefix;
        }

        public String getBodyPrefix() {
            return bodyPrefix;
        }
    }