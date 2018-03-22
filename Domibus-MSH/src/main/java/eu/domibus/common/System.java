package eu.domibus.common;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */

@Component
public class System {

    private boolean ready;

    @PostConstruct
    public void init(){
        ready =true;
    }

    public boolean isReady() {
        return ready;
    }
}
