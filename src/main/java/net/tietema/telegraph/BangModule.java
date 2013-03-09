package net.tietema.telegraph;

import com.google.inject.AbstractModule;
import com.squareup.otto.Bus;

/**
 * @author jeroen
 */
public class BangModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Bus.class).toProvider(EventBusProvider.class);
    }
}
