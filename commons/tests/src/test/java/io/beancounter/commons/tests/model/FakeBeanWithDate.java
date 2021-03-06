package io.beancounter.commons.tests.model;

import org.joda.time.DateTime;
import io.beancounter.commons.tests.annotations.Random;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FakeBeanWithDate {

    private DateTime date;

    @Random(names = { "date" })
    public FakeBeanWithDate(DateTime date) {
        this.date = date;
    }

    public DateTime getDate() {
        return date;
    }
}
