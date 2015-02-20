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

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;

public class MainActivity extends Activity
{
	EditText mOperatorInput;
	ToggleButton mToggler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mOperatorInput = (EditText) findViewById(R.id.operator);
		mToggler = (ToggleButton) findViewById(R.id.toggler);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		updateStatus();

		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		mOperatorInput.setText(sp.getString("operator", ""));
		if (mOperatorInput.getEditableText().length() == 0) {
			mOperatorInput.setHint(Util.getprop("gsm.operator.alpha"));
		}

		mOperatorInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					sp.edit().putString("operator", tv.getEditableText().toString()).commit();
					Util.setOperatorNameFromSettings(MainActivity.this);
					tv.clearFocus();
					tv.postDelayed(new Runnable() {

						@Override
						public void run() {
							updateStatus();
						}
					}, 500);

					return false;
				}

				return false;
			}
		});

		mToggler.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton button, boolean checked)
			{
				mOperatorInput.setEnabled(checked);
				sp.edit().putBoolean("enabled", checked).commit();
			}
		});

		final boolean enabled = sp.getBoolean("enabled", false);

		mToggler.setChecked(enabled);
		mOperatorInput.setEnabled(enabled);
	}

	private void updateStatus()
	{
		final String status =
				"gsm.sim.operator.alpha=" +
				Util.getprop("gsm.sim.operator.alpha") + "\n" +
				"gsm.operator.alpha=" +
				Util.getprop("gsm.operator.alpha") + "\n" +
				"gsm.operator.isroaming=" +
				Util.getprop("gsm.operator.isroaming");

		final TextView tv = (TextView) findViewById(R.id.status);
		tv.setText(status);
		tv.setTypeface(Typeface.MONOSPACE);
	}
}
