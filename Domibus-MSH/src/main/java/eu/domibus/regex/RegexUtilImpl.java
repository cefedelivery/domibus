package eu.domibus.regex;

import eu.domibus.api.regex.RegexUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Cosmin Baciu on 31-Aug-16.
 */
@Component
public class RegexUtilImpl implements RegexUtil {

    protected Map<String, Pattern> patternHashMap = new HashMap<>();

    @Override
    public boolean matches(String regex, String input) {
        Pattern pattern = getPattern(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    protected Pattern getPattern(String regex) {
        if (patternHashMap.containsKey(regex)) {
            return patternHashMap.get(regex);
        }
        Pattern pattern = Pattern.compile(regex);
        patternHashMap.put(regex, pattern);
        return pattern;
    }
}
