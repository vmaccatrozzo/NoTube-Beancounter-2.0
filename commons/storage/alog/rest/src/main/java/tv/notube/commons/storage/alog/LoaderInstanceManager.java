package tv.notube.commons.storage.alog;

import org.apache.log4j.Logger;
import tv.notube.commons.storage.model.ActivityLog;

import java.util.Properties;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class LoaderInstanceManager {

    private static LoaderInstanceManager instance =
            new LoaderInstanceManager();

    private ActivityLog activityLog;

    private Logger logger = Logger.getLogger(LoaderInstanceManager.class);

    public static LoaderInstanceManager getInstance() {
        if (instance == null)
            instance = new LoaderInstanceManager();

        return instance;
    }

    private LoaderInstanceManager() {
        Properties properties = new Properties();
        properties.setProperty("url", "jdbc:mysql://cibionte.cybion.eu:3306/collective-alog");
        properties.setProperty("username", "collective");
        properties.setProperty("password", "collective");
        activityLog = new DefaultActivityLogImpl(properties);

    }

    public ActivityLog getActivityLog() {
        return activityLog;
    }
}
