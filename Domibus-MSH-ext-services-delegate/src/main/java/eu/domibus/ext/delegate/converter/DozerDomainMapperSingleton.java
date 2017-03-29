package eu.domibus.ext.delegate.converter;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import java.util.ArrayList;
import java.util.List;

public class DozerDomainMapperSingleton {

    private static DozerBeanMapper instance;

    private DozerDomainMapperSingleton() {
    }

    public static Mapper getInstance() {
        if (instance == null) {
            instance = new DozerBeanMapper();
            List<String> mappingFiles = new ArrayList<>();
            mappingFiles.add("CompassDomainBeanMapping.xml");
            instance.setMappingFiles(mappingFiles);
        }
        return instance;
    }

}
