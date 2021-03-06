/*
 * Telegraph is an online messaging app with strong focus on privacy
 * Copyright (C) 2013 Jeroen Tietema <jeroen@tietema.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        if (instance == null) {
            instance = new Bus(); // injection doesn't happen on main thread?!?
        }
        return instance;
    }
}
