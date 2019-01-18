package eu.domibus.submission.plugin;

import eu.domibus.plugin.transformer.PluginHandler;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
public interface PluginHandlerProvider {

    PluginHandler getPluginHandler(String backendName);
}
