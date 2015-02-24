package at.jclehner.noroaming;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ListenerService extends Service
{
	private PhoneStateListener mListener;
	private TelephonyManager mTelMgr;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);

		mListener = new PhoneStateListener() {
			@Override
			public void onDataConnectionStateChanged(int state)
			{
				Util.setOperatorNameFromSettings(getApplicationContext());
				Log.i("NoRoaming", "Data connection state: " + state);
			}

			public void onServiceStateChanged(ServiceState serviceState)
			{
				Util.setOperatorNameFromSettings(getApplicationContext());
				Log.i("NoRoaming", "Service state: " + serviceState);
			}
		};

		mTelMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mTelMgr.listen(mListener,
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
				PhoneStateListener.LISTEN_SERVICE_STATE);

		Log.i("NoRoaming", "Listener service started");

		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mTelMgr.listen(mListener, PhoneStateListener.LISTEN_NONE);
		Log.i("NoRoaming", "Listener service stopped");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public static void start(Context context)
	{
		final Intent intent = new Intent(context, ListenerService.class);
		context.startService(intent);
	}
}
