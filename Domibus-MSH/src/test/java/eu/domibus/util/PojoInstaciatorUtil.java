package eu.domibus.util;


import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dussath on 5/19/17.
 * <p>
 * Util class that allowing pojo instanciation with some values.
 */
public class PojoInstaciatorUtil {

    private static final Logger logger = Logger.getLogger("PojoInstaciatorUtil");
    private static Pattern fieldPatern = Pattern.compile("(?<fieldName>\\w*):(?<fieldValue>\\w*)");
    private static Pattern ententityPattehrn = Pattern.compile("(?<entity>\\w*)\\[(?<parameter>.*)\\]");
    private static Pattern rootEntityPattern = Pattern.compile("(?<entity> )\\[(?<parameter>.*)\\]");
    private static Pattern collectionPattern = Pattern.compile("(?<entity>\\w*)\\{(?<parameterGroup>.*)\\}");

    public static <T> T instanciate(final Class<T> clazz, String... fieldValues) {
        HashMap<String, Object> parameters = new HashMap<>();
        if (fieldValues != null) {
            for (String fieldValue : fieldValues) {
                Map<String, Object> configuration = setUpParameters(fieldValue);
                parameters.putAll(configuration);
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
                                Set<Object> set = new HashSet<>();
                                if (o != null) {
                                    Map<String, Object> tmpMap = (Map<String, Object>) o;
                                    ParameterizedType genericType = (ParameterizedType) f.getGenericType();
                                    Class<?> type = (Class<?>) genericType.getActualTypeArguments()[0];
                                    for (String key : tmpMap.keySet()) {
                                        Map o1 = (Map) tmpMap.get(key);
                                        Object instanciate = instanciate(type, o1);
                                        set.add(instanciate);
                                    }
                                }
                                logger.info("Set " + f.getName());
                                f.set(instance, set);
                            } else if (fieldClass.isAssignableFrom(List.class)) {
                                logger.info("List " + f.getName());
                                f.set(instance, new ArrayList<>());
                            } else if (fieldClass.isAssignableFrom(Map.class)) {
                            } else if (fieldClass.isEnum()) {
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


        //Pattern ententityPattehrn=Pattern.compile("\\[(,?(?<parameter>[a-zA-Z]*:[a-zA-Z]*),?)*\\]");
        //Pattern ententityPattehrn=Pattern.compile("\\[([a-zA-Z]*:[a-zA-Z]*)?(,([a-zA-Z]*:[a-zA-Z]*),?)*\\]");

        setUpParameters("mep[name:oneway,value:http]");
        setUpParameters("responderParties{[name:oneway,value:http],[name:cool,userName:test]}");
//        System.out.println("Parameter "+matcher.group("parameter"));
        //   Assert.assertEquals("oneway",process.getMep());
        // Assert.assertEquals("push",process.getMepBinding());

        /*String test = "test1/test2[name:test,Class/name:name]";
        HashMap valueMap = new HashMap();
        setUpParameters(valueMap, test);
        System.out.println("Done");*/
    }


    static Map<String, Object> setUpParameters(String configuration) {
        Map<String, Object> configurationMap = new HashMap<>();
        return setUpParameters(configuration, configurationMap);
    }

    private static Map<String, Object> setUpParameters(String configuration, Map<String, Object> configurationMap) {
        matchRootEntity(configuration, configurationMap);
        matchEntity(configuration, configurationMap);
        matchCollections(configuration, configurationMap);
        return configurationMap;
    }

    private static void matchCollections(String configuration, Map<String, java.lang.Object> configurationMap) {
        Matcher collectionMatcher = collectionPattern.matcher(configuration);
        if (collectionMatcher.find()) {
            String entity = collectionMatcher.group("entity");
            if (entity.isEmpty()) return;
            String entityParameters = collectionMatcher.group("parameterGroup");
            String[] split = entityParameters.split(";");
            int count = 0;
            HashMap<String, Object> entityConfig = new HashMap<>();
            configurationMap.put(entity, entityConfig);
            for (String entityParameter : split) {
                String key = entity + "_" + count++;
                Map<String, Object> value = setUpParameters(key + entityParameter);
                entityConfig.put(key, value.get(key));
            }

        }
    }

    private static void matchRootEntity(String configuration, Map<String, Object> configurationMap) {
        Matcher entityMatcher = rootEntityPattern.matcher(configuration);
        if (entityMatcher.find()) {
            String entity = entityMatcher.group("entity");
            if (" ".equals(entity)) {
                String parameters = entityMatcher.group("parameter");
                String[] fields = parameters.split(",");
                logger.info("Entity " + entity);
                for (String field : fields) {
                    entityMatcher = fieldPatern.matcher(field);
                    if (entityMatcher.find()) {
                        configurationMap.put(entityMatcher.group("fieldName"), entityMatcher.group("fieldValue"));
                    }
                }
            }
        }
    }

    private static void matchEntity(String configuration, Map<String, Object> configurationMap) {
        Matcher entityMatcher = ententityPattehrn.matcher(configuration);
        if (entityMatcher.matches()) {
            String entity = entityMatcher.group("entity");
            if (entity.isEmpty()) return;
            HashMap<String, Object> parameterMap = new HashMap<>();
            configurationMap.put(entity, parameterMap);
            String parameters = entityMatcher.group("parameter");
            int startIndexSearch=0;
            int commaIndex=1;
            while (commaIndex!=-1) {
                commaIndex = parameters.indexOf(",", startIndexSearch);
                String field;
                if(commaIndex==-1){
                    field = parameters.substring(startIndexSearch);
                }
                else {
                    field = parameters.substring(startIndexSearch, commaIndex);
                }
                Matcher fieldMatcher =  fieldPatern.matcher(field);
                if (fieldMatcher.matches()) {
                    startIndexSearch=commaIndex+1;
                    parameterMap.put(fieldMatcher.group("fieldName"), fieldMatcher.group("fieldValue"));
                }else{
                    String subClassConfiguration = parameters.substring(startIndexSearch);
                    setUpParameters(subClassConfiguration,parameterMap);
                    commaIndex=-1;
                }
            }
            }
        }
    }


