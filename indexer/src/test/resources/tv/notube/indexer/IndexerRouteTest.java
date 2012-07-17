package tv.notube.indexer;

import java.io.IOException;
import java.util.UUID;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tv.notube.activities.ActivityStore;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;
import tv.notube.profiler.Profiler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IndexerRouteTest extends CamelTestSupport {
    private Injector injector;
    private Profiler profiler;
    private ActivityStore activityStore;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(IndexerRoute.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        injector = Guice.createInjector(new Module() {

            @Override
            public void configure(Binder binder) {
                activityStore = mock(ActivityStore.class);
                profiler = mock(Profiler.class);
                binder.bind(ActivityStore.class).toInstance(activityStore);
                binder.bind(Profiler.class).toInstance(profiler);

                binder.bind(IndexerRoute.class).toInstance(new IndexerRoute() {
                    @Override
                    public String fromKestrel() {
                        return "direct:start";
                    }

                    @Override
                    public String errorEndpoint() {
                        return "mock:error";
                    }
                });
            }
        });

        super.setUp();
    }


    @Test
    public void activityReachesBothServices() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        String json = activityAsJson();

        template.sendBody("direct:start", json);

        error.assertIsSatisfied();
        verify(activityStore).store(any(UUID.class), any(Activity.class));
        verify(profiler).profile(any(UUID.class), any(Activity.class));
    }

    @Test
    public void logsErrorFromOneOfTheServices() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(1);

        doThrow(new RuntimeException("problem")).when(activityStore)
                .store(any(UUID.class), any(Activity.class));
        template.sendBody("direct:start", activityAsJson());

        error.assertIsSatisfied();
        verify(activityStore).store(any(UUID.class), any(Activity.class));
        verify(profiler).profile(any(UUID.class), any(Activity.class));
    }

    private String activityAsJson() throws TestsException, IOException {
        UUID userId = UUID.randomUUID();
        Activity activity = TestsBuilder.getInstance().build().build(Activity.class).getObject();
        ResolvedActivity resolvedActivity = new ResolvedActivity(userId, activity);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(resolvedActivity);
    }
}