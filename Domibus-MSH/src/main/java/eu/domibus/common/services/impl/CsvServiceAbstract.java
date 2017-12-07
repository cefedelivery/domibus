package eu.domibus.common.services.impl;

import eu.domibus.common.services.CsvService;
import org.springframework.stereotype.Service;

@Service
public abstract class CsvServiceAbstract implements CsvService {

    protected String uncamelcase(String str) {
        String result = str.replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2");
        return result.substring(0,1).toUpperCase() + result.substring(1);
    }
}
