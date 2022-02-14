package com.playtomic.tests.wallet.utils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.doAnswer;

public class Stubber {

    public static org.mockito.stubbing.Stubber doSleep(Duration timeUnit) {
        return doAnswer(invocationOnMock -> {
            TimeUnit.MILLISECONDS.sleep(timeUnit.toMillis());
            return null;
        });
    }

    public static <E> org.mockito.stubbing.Stubber doSleep(Duration timeUnit, E ret) {
        return doAnswer(invocationOnMock -> {
            TimeUnit.MILLISECONDS.sleep(timeUnit.toMillis());
            return ret;
        });
    }

}
