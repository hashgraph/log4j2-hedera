package com.hedera;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;


@Plugin(name = "Log4jHedera", category = "Core", elementType = "appender", printObject = true)
public class Log4jHederaAppender extends AbstractAppender
{
    public static final String APPENDER_NAME = "log4j-hedera";
    public static final String LOG4J_NDC = "log4j-NDC";
    public static final String LOG4J_MARKER = "log4j-Marker";
    public static final String THREAD_NAME = "log4j-Threadname";

    public Log4jHederaAppender() {
        this(APPENDER_NAME);
    }

    protected Log4jHederaAppender(String name) 
    {
        super(name, null, null, true, null);
    }

    @PluginFactory
    public static Log4jHederaAppender createAppender(@PluginAttribute("name") final String name) 
    {
        if (name == null) {
            LOGGER.error("No name provided");
            return null;
        }
        return new Log4jHederaAppender(name);
    }

    //Receive LogEvent and send to HCS
    @Override
    public void append(LogEvent logEvent) {
        System.out.println("hello");
        System.out.print(logEvent.getMessage());
    }
}