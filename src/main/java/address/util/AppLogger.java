package address.util;

import address.events.BaseEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class AppLogger {
    private Logger logger;

    public AppLogger(Logger logger) {
        this.logger = logger;
    }

    public void info(String message, Object... params) {
        logger.info(message, params);
    }

    public void debug(String message, Object... params) {
        logger.debug(message, params);
    }

    public void trace(String message, Object... params) {
        logger.trace(message, params);
    }

    public void warn(String message, Object... params) {
        logger.warn(message, params);

    }

    public void error(String message, Object... params) {
        logger.error(message, params);
    }

    public void fatal(String message, Object... params) {
        logger.fatal(message, params);
    }

    public <T extends Throwable> void throwing(T throwable) {
        logger.throwing(Level.DEBUG, throwable);
    }

    public <T extends Throwable> void catching(T throwable) {
        logger.catching(Level.DEBUG, throwable);
    }

    public <T extends BaseEvent> void debugEvent(T event) {
        debugEvent("{}: {}", event.getClass().getSimpleName(), event.toString());
    }

    // this method is required since debug(message, obj, obj) seems to be problematic
    private void debugEvent(String message, Object... params) {
        logger.debug(message, params);
    }

    public void infoEvent(BaseEvent event) {
        logger.info("{}: {}", event.getClass().getSimpleName(), event.toString());
    }

    // this method is required since info(message, obj, obj) seems to be problematic
    private void infoEvent(String message, Object... params) {
        logger.info(message, params);
    }
}