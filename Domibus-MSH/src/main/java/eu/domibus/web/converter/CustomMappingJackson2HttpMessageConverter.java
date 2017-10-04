package eu.domibus.web.converter;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class CustomMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public void setJsonPrefix(String jsonPrefix) {
        super.setJsonPrefix(fixNewLineCharacter(jsonPrefix));
    }

    protected String fixNewLineCharacter(String text) {
        return StringUtils.replace(text, "\\n", "\n");
    }
}
