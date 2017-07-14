package eu.domibus.web.rest;

import eu.domibus.web.rest.ro.LoginRO;
import eu.domibus.web.rest.ro.UserRO;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class TestResourceTest {

    @Tested
    TestResource testResource;

    @Test
    public void testTestGet() {
        // Given

        // When
        UserRO userRO = testResource.testGet();

        // Then
        Assert.assertNotNull(userRO);
        Assert.assertEquals("testGet", userRO.getUsername());
    }

    @Test
    public void testTestPost() {
        // Given
        LoginRO loginRO = new LoginRO();
        HttpServletRequest request = new MockHttpServletRequest("post", "http://testPost");

        // When
        UserRO userRO = testResource.testPost(loginRO, request);

        // Then
        Assert.assertNotNull(userRO);
        Assert.assertEquals("testPost", userRO.getUsername());
    }
}
