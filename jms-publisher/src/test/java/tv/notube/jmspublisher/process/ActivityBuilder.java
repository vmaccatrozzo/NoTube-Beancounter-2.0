package tv.notube.jmspublisher.process;

import java.io.IOException;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;

import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.commons.model.auth.OAuthAuth;
import tv.notube.commons.model.randomisers.VerbRandomizer;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;

public class ActivityBuilder {


    public static String resolvedActivityAsJson() throws TestsException, IOException {
        ResolvedActivity resolvedActivity = aResolvedActivity();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(resolvedActivity);
    }

    public static ResolvedActivity aResolvedActivity() {
        UUID userId = UUID.randomUUID();
        Activity activity = anActivity();
        User user = new User();
        user.setName("test-name");
        user.setPassword("abracadabra");
        user.setSurname("test-surname");
        user.setUsername("test-username");
        user.addService("facebook", new OAuthAuth("test-session", "test-secret"));
        return new ResolvedActivity(userId, activity, user);
    }


    private static Activity anActivity() {
        try {
            TestsBuilder testsBuilder = TestsBuilder.getInstance();
            testsBuilder.register(new VerbRandomizer("verb-randomizer"));
            return testsBuilder.build().build(Activity.class).getObject();
        } catch (TestsException e) {
            throw new RuntimeException(e);
        }
    }
}
