package net.tietema.telegraph;

import android.app.Application;
import com.google.inject.AbstractModule;
import roboguice.inject.ContextScope;
import roboguice.inject.ResourceListener;
import roboguice.inject.ViewListener;

/**
 * @author jeroen
 */
public class BangModule extends AbstractModule {

    protected Application application;
    protected ContextScope contextScope;
    protected ResourceListener resourceListener;
    protected ViewListener viewListener;

    public BangModule(final Application application, ContextScope contextScope, ViewListener viewListener, ResourceListener resourceListener) {
        this.application = application;
        this.contextScope = contextScope;
        this.viewListener = viewListener;
        this.resourceListener = resourceListener;
    }
    @Override
    protected void configure() {
        //bind(BangApplication.class, BangApplicationProvider.class);
    }
}
