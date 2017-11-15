package eu.domibus.ext.delegate.converter;

import org.dozer.CustomConverter;

public class DataHandlerConverter implements CustomConverter{
    @Override
    public Object convert(Object o, Object o1, Class<?> aClass, Class<?> aClass1) {
        return o1;
    }
}
