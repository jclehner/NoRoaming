/*
 * NoRoaming - MVNO roaming fix
 * Copyright (C) 2015 Joseph C. Lehner
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of  MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.jclehner.noroaming;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import eu.chainfire.libsuperuser.Shell.SU;

public final class Util
{
	public static void setOperatorNameFromSettings(Context context)
	{
		final SharedPreferences sp =
				PreferenceManager.getDefaultSharedPreferences(context);

		if (!sp.getBoolean("enabled", false)) {
			return;
		}

		final String operator = sp.getString("operator", null);
		if (operator != null) {
			if (!operator.equals(getprop("gsm.sim.operator.alpha"))) {
				new Thread() {
					public void run()
					{
						final String cmd = "setprop gsm.sim.operator.alpha '" +
								operator.replace("'", "\\'") + "'";
						SU.run(cmd);
						Log.i("NoRoaming", "Operator set to " + operator);
					}
				}.start();
			}
		} else {
			Log.i("NoRoaming", "No operator specified");
		}
	}

	public static String getprop(String key) {
		return getprop(key, "");
	}

	public static String getprop(String key, String defValue)
	{
		try
		{
			final Class<?> clazz = Class.forName("android.os.SystemProperties");
			boolean has2Args = true;
			Method m;

			try {
				m = clazz.getMethod("get", String.class, String.class);
			} catch (NoSuchMethodException e) {
				has2Args = false;
				m = clazz.getMethod("get", String.class);
			}

			if (has2Args) {
				return (String) m.invoke(null, key, defValue);
			}

			final String ret = (String) m.invoke(null, key);
			if (ret.length() == 0 && defValue != null) {
				return defValue;
			}

			return ret;
		} catch (ClassNotFoundException e) {
			Log.w("NoRoaming", e);
		} catch (IllegalAccessException e) {
			Log.w("NoRoaming", e);
		} catch (IllegalArgumentException e) {
			Log.w("NoRoaming", e);
		} catch (InvocationTargetException e) {
			Log.w("NoRoaming", e);
		} catch (NoSuchMethodException e) {
			Log.w("NoRoaming", e);
		}

		return defValue != null ? defValue : "";
	}
}
