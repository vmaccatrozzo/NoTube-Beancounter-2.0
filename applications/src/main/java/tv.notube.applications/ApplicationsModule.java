package tv.notube.applications;

import com.google.inject.AbstractModule;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import com.google.inject.name.Names;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ApplicationsModule extends AbstractModule {

    @Override
    protected void configure() {
        Properties properties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), properties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
        bind(ApplicationsManager.class).to(JedisApplicationsManagerImpl.class);
    }
}
