package tv.notube.platform;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import tv.notube.activities.ActivityStore;
import tv.notube.activities.MockActivityStore;
import tv.notube.applications.MockApplicationsManager;
import tv.notube.crawler.Crawler;
import tv.notube.applications.ApplicationsManager;
import tv.notube.crawler.MockCrawler;
import tv.notube.profiles.MockProfiles;
import tv.notube.profiles.Profiles;
import tv.notube.queues.MockQueues;
import tv.notube.queues.Queues;
import tv.notube.usermanager.MockUserManager;
import tv.notube.usermanager.UserManager;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Enrico Candino (enrico.candino@gmail.com)
 */
public class TestServiceConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new JerseyServletModule() {
            @Override
            protected void configureServlets() {
                Map<String, String> initParams = new HashMap<String, String>();
                // add bindings to mockups
                bind(ApplicationsManager.class).to(MockApplicationsManager.class).asEagerSingleton();
                bind(UserManager.class).to(MockUserManager.class);
                bind(Profiles.class).to(MockProfiles.class);
                bind(Crawler.class).to(MockCrawler.class);
                bind(ActivityStore.class).to(MockActivityStore.class);
                bind(Queues.class).to(MockQueues.class);
                // add REST services
                bind(ApplicationService.class);
                bind(UserService.class);
                bind(ActivitiesService.class);
                // add bindings for Jackson
                bind(JacksonJaxbJsonProvider.class).asEagerSingleton();
                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
                // Route all requests through GuiceContainer
                serve("/*").with(GuiceContainer.class);
                filter("/*").through(GuiceContainer.class, initParams);
            }
        });
    }

}