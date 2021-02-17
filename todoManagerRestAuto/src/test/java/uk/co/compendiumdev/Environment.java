package uk.co.compendiumdev;

import uk.co.compendiumdev.utils.Port;

/**
 * Provided Source Code from EvilTester Repository.
 * All credits go to the author of the repository.
 */
public class Environment {
    public static String getEnv(String urlPath){
        return  getBaseUri() + urlPath;
    }

    public static String getBaseUri() {

        // if not running then start the spark
        if(Port.inUse("localhost", 4567)) {
            return "http://localhost:4567";
        }else{
            return null;
        }
    }
}
