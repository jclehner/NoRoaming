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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import eu.chainfire.libsuperuser.Shell.SU;

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

		ListenerService.start(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params)
			{
				return SU.available();
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (!result) {
					final AlertDialog.Builder ab =
							new AlertDialog.Builder(MainActivity.this);

					ab.setMessage(R.string.root_required);
					ab.setCancelable(false);
					ab.setNeutralButton(android.R.string.ok, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							MainActivity.this.finish();
						}
					});
					ab.show();
				}
			}
		}.execute();

		updateStatus();

		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

		String operator = sp.getString("operator", null);
		if (TextUtils.isEmpty(operator)) {
			operator = Util.getprop("gsm.operator.alpha");
		}

		if (TextUtils.isEmpty(operator)) {
			mOperatorInput.setHint(R.string.operator);
		}

		mOperatorInput.setText(operator);

		mOperatorInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					sp.edit().putString("operator", tv.getEditableText().toString()).commit();
					Toast.makeText(MainActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
					tv.clearFocus();

					setOperatorNameAndStatus();
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

				setOperatorNameAndStatus();
			}
		});

		final boolean enabled = sp.getBoolean("enabled", false);

		mToggler.setChecked(enabled);
		mOperatorInput.setEnabled(enabled);
	}

	private void setOperatorNameAndStatus()
	{
		Util.setOperatorNameFromSettings(this);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				updateStatus();
			}
		}, 500);
	}

	private void updateStatus()
	{
		final String[] props = {
				"gsm.sim.operator.alpha",
				"gsm.sim.operator.numeric",
				"gsm.operator.alpha",
				"gsm.operator.numeric",
				"gsm.operator.isroaming"
		};

		final StringBuilder sb = new StringBuilder();
		for (String prop : props) {
			sb.append(prop + "=" + Util.getprop(prop) + "\n");
		}

		final TextView tv = (TextView) findViewById(R.id.status);
		tv.setText(sb);
		tv.setTypeface(Typeface.MONOSPACE);
	}
}
