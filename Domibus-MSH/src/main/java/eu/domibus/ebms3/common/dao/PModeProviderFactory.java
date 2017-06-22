package eu.domibus.ebms3.common.dao;


import eu.domibus.common.dao.PModeDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class PModeProviderFactory  {

    private static final String CACHING_IMPLEMENATION = "CachingPModeProvider";
    private static final String PMODE_DAO = "PModeDao";
    private static final String DYNAMIC_DISCOVERY = "DynamicDiscoveryPModeProvider";

    private String implementation;

    public PModeProvider getObject() throws Exception {
        if(StringUtils.equals(CACHING_IMPLEMENATION, implementation)) {
            return new CachingPModeProvider();
        } else if(StringUtils.equals(DYNAMIC_DISCOVERY, implementation)) {
            return new DynamicDiscoveryPModeProvider();
        }
        //default implementation
        return new PModeDao();
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }
}
