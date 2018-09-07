package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.DomainException;
import eu.domibus.web.rest.error.RestControllerAdvice;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class RestControllerAdviceTest {
    
    private MockMvc mockMvc;

    @InjectMocks
    private RestControllerAdvice unitUnderTest;

    @Mock
    private PluginUserResource pluginUserResource;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(pluginUserResource)
                .setControllerAdvice(unitUnderTest)
                .build();
    }

    @Test
    public void testDomainExceptionHandler() throws Exception {

        String dummyPayload = "[]";
        String exceptionMessage = "aaa";
        String formattedException = " {\"message\": \"[DOM_001]:" + exceptionMessage + "\"} ";

        doThrow(new DomainException(exceptionMessage)).when(pluginUserResource).updateUsers(anyList());

        mockMvc.perform(
                put("/rest/plugin/users")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(dummyPayload)
        )
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(formattedException, false))
        ;
    }

    @Test
    public void testExceptionHandler() throws Exception {

        String dummyPayload = "[]";
        String exceptionMessage = "aaa";
        String formattedException = " {\"message\": \"" + exceptionMessage + "\"} ";

        doThrow(new IllegalArgumentException(exceptionMessage)).when(pluginUserResource).updateUsers(anyList());

        mockMvc.perform(
                put("/rest/plugin/users")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(dummyPayload)
        )
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(formattedException, false))
        ;
    }
}
