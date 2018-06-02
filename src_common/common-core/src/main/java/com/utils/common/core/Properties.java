package com.utils.common.core;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Util to access the app properties
 */
public abstract class Properties {

    private static final String PROPERTIES_FILE = "config.properties";

    private static final Logger logger = Logger.getLogger(Properties.class);

    private static java.util.Properties properties;

    private Properties() {

    }

    /**
     * Getter
     * @param propertyName property name
     * @return property value
     */
    public static Object get(String propertyName) {

        if(properties == null) {

            initProperties();
        }

        return properties.get(propertyName);
    }

    /**
     * Helper
     */
    private static void initProperties() {

        properties = new java.util.Properties();
        InputStream input = null;

        try {

            String filename = PROPERTIES_FILE;
            input = Properties.class.getClassLoader().getResourceAsStream(filename);
            if(input == null) {

                logger.log(Level.ERROR, "Cannot load " + filename);
                throw new IllegalStateException("Cannot load properties");
            }

            properties.load(input);
        }
        catch(IOException e) {

            logger.log(Level.ERROR, e);
            throw new IllegalStateException("Cannot load properties");
        }
        finally {

            if(input != null) {

                try {

                    input.close();
                }
                catch(IOException e) {

                    logger.log(Level.ERROR, e);
                }
            }
        }
    }
}
