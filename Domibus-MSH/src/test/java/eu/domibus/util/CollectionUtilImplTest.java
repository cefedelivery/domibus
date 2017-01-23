package eu.domibus.util;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@RunWith(JMockit.class)
public class CollectionUtilImplTest {

    @Tested
    CollectionUtilImpl collectionUtil;

    @Test
    public void testSafeSubListWithNullValue() throws Exception {
        final List<Object> result = collectionUtil.safeSubList(null, 1, 3);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testSafeSubListWithFromIndexBiggerThanTheCollectionSize() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("1");
        final List<String> result = collectionUtil.safeSubList(list, 5, 8);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testSafeSubListWithFromIndexBiggerThanTheToIndex() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("1");
        final List<String> result = collectionUtil.safeSubList(list, 10, 8);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testSafeSubListWithToIndexSmallerThanTheCollectionSize() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        final List<String> result = collectionUtil.safeSubList(list, 0, 3);
        assertFalse(result.isEmpty());
        assertEquals(result, Arrays.asList(new String[]{"1", "2", "3"}));
    }

    @Test
    public void testSafeSubListWithToIndexBiggerThanTheCollectionSize() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        final List<String> result = collectionUtil.safeSubList(list, 0, 10);
        assertFalse(result.isEmpty());
        assertEquals(result, list);
    }
}
