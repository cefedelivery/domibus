package eu.domibus.plugin.fs;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * File system observer.
 * Watches for and acts upon file system events on the outgoing folder.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSObserver {
    
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSObserver.class);
    
    private static final String OUTGOING_FOLDER_NAME = "OUT";
    
    @Resource(name = "fsPluginProperties")
    private FSPluginProperties fsPluginProperties;
    
    @Autowired
    @Qualifier("fsPluginObserverExecutor")
    private TaskExecutor taskExecutor;
    
    @PostConstruct
    public void init() {
        LOG.debug("FSObserver has started");
        LOG.debug("FSObserver Properties: {}", fsPluginProperties);
        
        setupListeners();
    }
    
    private void setupListeners() {
        try {
            Path dir = Paths.get(fsPluginProperties.getLocation(), OUTGOING_FOLDER_NAME);
            
            WatchService watcher = FileSystems.getDefault().newWatchService();
            WatchKey key = dir.register(watcher, ENTRY_CREATE);
            
            taskExecutor.execute(new FSObserverRunnable(dir, watcher));
        } catch (IOException ex) {
            LOG.error(null, ex);
        }

    }
    
    private static class FSObserverRunnable implements Runnable {
        
        private final Path dir;
        private final WatchService watcher;

        public FSObserverRunnable(Path dir, WatchService watcher) {
            this.dir = dir;
            this.watcher = watcher;
        }

        @Override
        public void run() {
            WatchKey key = null;
            boolean stopWatching = false;

            while (!stopWatching) {

                try {
                    LOG.debug("Taking from watcher");
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return;
                }

                List<WatchEvent<?>> events = key.pollEvents();
                LOG.debug("Got {} FS events", events.size());
                
                for (WatchEvent<?> event : events) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // This key is registered only
                    // for ENTRY_CREATE events,
                    // but an OVERFLOW event can
                    // occur regardless if events
                    // are lost or discarded.
                    if (kind == OVERFLOW) {
                        LOG.warn("File system watcher has overflown");
                        continue;
                    }

                    // The filename is the
                    // context of the event.
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    LOG.debug("Got FS event for filename: {}", filename);

                    // This is just example code from the tutorial
                    // Might be useful later
                    // Verify that the new file is a text file.
                    try {
                        // Resolve the filename against the directory.
                        // If the filename is "test" and the directory is "foo",
                        // the resolved name is "test/foo".
                        Path child = dir.resolve(filename);
                        if (!Files.probeContentType(child).equals("text/plain")) {
                            LOG.info("New file {} is not a plain text file.", filename);
                            continue;
                        }
                    } catch (IOException x) {
                        System.err.println(x);
                        continue;
                    }

                    // do things with file
                }

                // Reset the key -- this step is critical if you want to
                // receive further watch events.  If the key is no longer valid,
                // the directory is inaccessible so exit the loop.
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }
    
}
