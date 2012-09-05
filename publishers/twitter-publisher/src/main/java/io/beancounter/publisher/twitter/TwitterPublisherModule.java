package io.beancounter.publisher.twitter;

import com.google.inject.Provides;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class TwitterPublisherModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        TwitterFactory factory = new TwitterFactory();
        bind(Twitter.class).toInstance(factory.getInstance());
        bind(TwitterPublisherRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }
}