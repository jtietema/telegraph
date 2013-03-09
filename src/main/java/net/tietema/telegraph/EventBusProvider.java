package net.tietema.telegraph;

import com.google.inject.Provider;
import com.squareup.otto.Bus;

/**
 * @author jeroen
 */
public class EventBusProvider implements Provider<Bus> {

    private static Bus instance;

    @Override
    public Bus get() {
        if (instance == null)
            instance = new Bus(); // injection doesn't happen on main thread?!?
        return instance;
    }
}
