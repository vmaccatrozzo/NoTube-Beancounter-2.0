package io.beancounter.resolver.process;

import java.util.UUID;

import com.google.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.resolver.Resolver;
import io.beancounter.usermanager.UserManager;

/**
 * This class is the <i>Camel</i> route which orchestrates all the resolver flow.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ResolverRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverRoute.class);

    @Inject
    private Resolver resolver;

    @Inject
    UserManager userManager;

    public void configure() {
        errorHandler(deadLetterChannel(errorEndpoint()));

        from(fromEndpoint())
                // ?concurrentConsumers=10&waitTimeMs=500
                .convertBodyTo(String.class)
                .unmarshal().json(JsonLibrary.Jackson, Activity.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Activity activity = exchange.getIn().getBody(Activity.class);
                        LOGGER.debug("resolving username {}.", activity);
                        UUID userId = resolver.resolve(activity);
                        String username = resolver.resolveUsername(
                                activity.getContext().getUsername(),
                                activity.getContext().getService()
                        );
                        if (userId == null || username == null) {
                            exchange.getIn().setBody(null);
                        } else {
                            User user = userManager.getUser(username);
                            exchange.getIn().setBody(
                                    new ResolvedActivity(userId, activity, user)
                            );
                        }
                        LOGGER.debug("resolved username [{}-{}].", activity.getContext().getUsername(), userId);
                    }
                })
                .filter(body().isNotNull())
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class)
                .to(toInternalQueue());
    }

    protected String toInternalQueue() {
        return "kestrel://{{kestrel.queue.internal.url}}";
    }

    protected String fromEndpoint() {
        return "kestrel://{{kestrel.queue.social.url}}";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }
}