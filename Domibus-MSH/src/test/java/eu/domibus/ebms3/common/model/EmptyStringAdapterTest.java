package eu.domibus.ebms3.common.model;

import mockit.Tested;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EmptyStringAdapterTest {

    @Tested
    EmptyStringAdapter emptyStringAdapter;

    @Test
    public void testUnmarshalling() {
        String emptyString = "";
        String spaceString = " ";
        String spacesString = "  ";
        String notTrimmedString = " test\ntest\ttest ";
        String trimmedString = "test test test";

        assertEquals(null, emptyStringAdapter.unmarshal(null));
        assertEquals(" ", emptyStringAdapter.unmarshal(emptyString));
        assertEquals(" ", emptyStringAdapter.unmarshal(spaceString));
        assertEquals(" ", emptyStringAdapter.unmarshal(spacesString));
        assertEquals(trimmedString, emptyStringAdapter.unmarshal(notTrimmedString));
    }

    @Test
    public void testMarshalling() {
        String emptyString = "";
        String spaceString = " ";

        assertEquals(null, emptyStringAdapter.marshal(null));
        assertEquals("", emptyStringAdapter.marshal(emptyString));
        assertEquals("", emptyStringAdapter.marshal(spaceString));
    }

}
