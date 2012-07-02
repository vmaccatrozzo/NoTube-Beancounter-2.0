package tv.notube.crawler;

import org.joda.time.DateTime;
import tv.notube.usermanager.UserManager;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */

public class MockCrawler implements Crawler {

    @Override
    public Report crawl() throws CrawlerException {
        return new Report(1, DateTime.now().getMillis(), DateTime.now().getMillis());
    }

    @Override
    public Report crawl(String username) throws CrawlerException {
        return new Report(1, DateTime.now().getMillis(), DateTime.now().getMillis());
    }

    @Override
    public UserManager getUserManager() {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public boolean isCompleted() {
        throw new UnsupportedOperationException("NIY");
    }
}