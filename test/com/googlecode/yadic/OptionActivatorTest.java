package com.googlecode.yadic;

import com.googlecode.totallylazy.Option;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class OptionActivatorTest {
    private Container resolver = new SimpleContainer().addActivator(String.class, new Callable<String>() {
        public String call() throws Exception {
            throw new ContainerException("",new NoSuchElementException());
        }
    }).add(WithConstructor.class);

    @Test
    public void shouldReturnNoneIfCauseStackContainsNoSuchElementException() throws Exception {
        OptionActivator activator = new OptionActivator(WithConstructor.class, resolver);
        assertThat(activator.call().isEmpty(), is(true));
    }

    public static class WithConstructor {
        public WithConstructor(String value) {
        }
    }
}
