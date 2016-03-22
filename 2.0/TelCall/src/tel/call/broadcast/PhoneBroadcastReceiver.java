package tel.call.broadcast;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class PhoneBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = PhoneBroadcastReceiver.class
			.getSimpleName();

	private OnePhoneStateListener listener;

	@Override
	public void onReceive(Context ctx, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			// TODO
			TelephonyManager _tm = (TelephonyManager) ctx
					.getSystemService(Service.TELEPHONY_SERVICE);
			// TODO
			if (null == listener)
				listener = new OnePhoneStateListener();
			// TODO
			_tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		}
	}

	@Override
	public void finalize() throws Throwable {
		if (null != listener)
			listener = null;
		super.finalize();
	}

	private class OnePhoneStateListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: // 挂断
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: // 接听
				break;
			case TelephonyManager.CALL_STATE_RINGING: // 响铃
				break;
			}
		}
	};
}
