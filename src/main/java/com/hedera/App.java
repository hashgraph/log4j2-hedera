package com.hedera;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.hedera.Log4jHederaAppender;

public class App 
{
    final static Logger logger = LogManager.getLogger(App.class);
    public static void main( String[] args )
    {
        App obj = new App();
        obj.runMe("error message");        
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
