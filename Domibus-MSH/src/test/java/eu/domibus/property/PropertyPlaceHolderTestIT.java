package eu.domibus.property;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by baciuco on 08/08/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/propertyResolverContext.xml")
public class PropertyPlaceHolderTestIT {

    @Value("${mycustomKey}")
    String value;

    @Value("${mycustomKey1}")
    String myProperty1;

    @Value("${mykey:${mycustomKey}}/work")
    String value1;

    @Test
    public void testResolveProperty() throws Exception {
        Assert.assertNotNull(value);
        Assert.assertEquals(value, "mycustomvalue");

        System.out.println("value1=" + value1);
        Assert.assertNotNull(value1);


        System.out.println("mycustomKey1=" + myProperty1);
        Assert.assertNotNull(myProperty1);
    }

}
