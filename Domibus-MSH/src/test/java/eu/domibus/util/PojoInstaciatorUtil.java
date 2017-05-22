package eu.domibus.util;

import eu.domibus.common.model.configuration.Process;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by dussath on 5/19/17.
 *
 * Util class that allowing pojo instanciation with some values.
 */
public class PojoInstaciatorUtil {

    private static final Logger logger = Logger.getLogger("PojoInstaciatorUtil");

    public static <T> T instanciateProcess(final Class<T> clazz, String... fieldValues) {
        HashMap<String, Object> parameters = new HashMap<>();
        if (fieldValues != null) {
            for (String fieldValue : fieldValues) {
                setUpParameters(parameters, fieldValue);
            }
        }
        return instanciate(clazz, parameters);
    }

    private static <T> T instanciate(Class<T> clazz, Map<String, Object> parameters) {
        try {
            T instance = clazz.newInstance();
            Field[] fields = clazz.getDeclaredFields();
            logger.info(clazz.getName());
            for (Field f : fields) {
                Class fieldClass = f.getType();
                if (!fieldClass.isPrimitive()) {
                    if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())
                            && !java.lang.reflect.Modifier.isTransient(f.getModifiers())) {
                        f.setAccessible(true);
                        Object o = parameters.get(f.getName());
                        if (fieldClass.isAssignableFrom(String.class)) {
                            logger.info("String " + f.getName());
                            String fieldValue = "Mock";
                            if (o != null) {
                                fieldValue = (String) o;
                            }
                            f.set(instance, fieldValue);
                        } else {
                            if (fieldClass.isAssignableFrom(Set.class)) {
                                logger.info("Set " + f.getName());
                                f.set(instance, new HashSet<>());
                            } else if (fieldClass.isAssignableFrom(List.class)) {
                                logger.info("List " + f.getName());
                                f.set(instance, new ArrayList<>());
                            } else {
                                Map<String, Object> tmpMap = new HashMap();
                                if (o != null) {
                                    tmpMap = (Map<String, Object>) o;
                                }
                                logger.info("Other " + f.getName());
                                f.set(instance, instanciate(fieldClass, tmpMap));
                            }
                        }
                        f.setAccessible(false);
                    }
                }
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Process process = PojoInstaciatorUtil.instanciateProcess(Process.class, "mep/name:oneway", "mepBinding/name:push");
        logger.info("Mep name " + process.getMep().getName());
        logger.info("Binding name " + process.getMepBinding().getName());

       /* String test = "test1/test2/test3:cool";
        HashMap valueMap = new HashMap();
        setUpParameters(valueMap, test);
        System.out.println("Done");*/
    }

    private static void setUpParameters(Map map, String values) {
        String[] split = values.split("/");
        if (split.length >= 2) {
            HashMap<String, Object> valueMap = new HashMap<>();
            map.put(split[0], valueMap);
            values = values.substring(values.indexOf("/") + 1);
            setUpParameters(valueMap, values);
        } else if (split.length == 1) {
            split = values.split(":");
            if (split.length != 2) {
                throw new RuntimeException("Invalid configuration string");
            }
            map.put(split[0], split[1]);
        }
    }


}
