package org.flennn.lightdeathmessages.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class DebugLogger {
    private final Set<String> warned = Collections.synchronizedSet(new HashSet<>());
    private volatile boolean debug;
    private volatile boolean warnOnce;

    public DebugLogger(boolean debug, boolean warnOnce) {
        this.debug = debug;
        this.warnOnce = warnOnce;
    }

    public void configure(boolean debug, boolean warnOnce) {
        this.debug = debug;
        this.warnOnce = warnOnce;
        if (!warnOnce) {
            warned.clear();
        }
    }

    public void debug(String message) {
        if (debug) {
            Console.info("[Debug] " + message);
        }
    }

    public void warn(String key, String message) {
        if (!warnOnce || warned.add(key)) {
            Console.warn(message);
        }
    }
}
