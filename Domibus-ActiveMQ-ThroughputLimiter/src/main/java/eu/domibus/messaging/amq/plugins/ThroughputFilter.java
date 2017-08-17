
package eu.domibus.messaging.amq.plugins;

import javax.jms.Queue;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class ThroughputFilter {
    private String queue;
    private String propertyKey;
    private String name;
    private int maxParallel;


    public int getMaxParallel() {
        return maxParallel;
    }

    public void setMaxParallel(final int maxParallel) {
        this.maxParallel = maxParallel;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(final String queue) {
        this.queue = queue;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(final String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }


}
