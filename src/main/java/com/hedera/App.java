package com.hedera;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class App 
{
    final static Logger logger = LogManager.getLogger(App.class);
    public static void main( String[] args )
    {
        App app = new App();
        app.runMe("No receipts");        
    }

    private void runMe(String parameter)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("This is debug: " + parameter);
        }

        if(logger.isInfoEnabled())
        {
			logger.info("This is info : " + parameter);
		}
		
		logger.warn("This is warn : " + parameter);
		logger.error("This is error : " + parameter);
		logger.fatal("This is fatal : " + parameter);
    }
}
