package org.swssf.policy;

import org.apache.ws.secpolicy.WSSPolicyException;
import org.swssf.wss.securityEvent.SecurityEvent;

/**
 * User: giger
 * Date: 11/26/11
 * Time: 11:07 AM
 * Copyright 2011 Marc Giger gigerstyle@gmx.ch
 * <p/>
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2, or (at your option) any
 * later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
public interface Assertable {

    SecurityEvent.Event[] getSecurityEventType();

    boolean assertEvent(SecurityEvent securityEvent) throws WSSPolicyException;

    boolean isAsserted();

    String getErrorMessage();

}