package eu.domibus.ebms3.common.model;

import eu.domibus.util.SoapUtil;
import mockit.Tested;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Node;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ToStringAdapterTest {

    @Tested
    ToStringAdapter toStringAdapter;

    @Test
    public void testToStringToNode() throws IOException, TransformerException {

        final String receiptPath = "dataset/as4/MSHAS4Response.xml";
        String receipt = IOUtils.toString(new ClassPathResource(receiptPath).getInputStream());

        Node node = toStringAdapter.stringToNode(receipt);
        Node resultNode = toStringAdapter.stringToNode(toStringAdapter.nodeToString(node));
        assertEquals(node.getTextContent(), resultNode.getTextContent());
    }
}
