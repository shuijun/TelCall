package tel.call.broadcast;

import java.util.Date;

import tel.call.util.AppUtil;
import tel.call.util.UserInfo;
import tel.call.util.UserInfo.CallInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class PhoneBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = PhoneBroadcastReceiver.class
			.getSimpleName();

	private static final String[] porjection = new String[] { Calls.TYPE,
			Calls.NUMBER, Calls.DATE, Calls.DURATION };

	private OnePhoneStateListener listener;

	private UserInfo userInfo;
	private SharedPreferences preferences;

	@SuppressWarnings("static-access")
	@Override
	public void onReceive(Context ctx, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			// TODO
			userInfo = (UserInfo) ctx.getApplicationContext();
			preferences = ctx.getSharedPreferences(AppUtil.UN_UPLOAD,
					ctx.getApplicationContext().MODE_PRIVATE);

			// TODO
			String tel_num = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			// TODO
			TelephonyManager _tm = (TelephonyManager) ctx
					.getSystemService(Service.TELEPHONY_SERVICE);
			// TODO
			if (null == listener)
				listener = new OnePhoneStateListener(ctx);
			// TODO
			listener.setTel_num(tel_num);
			listener.setCall_time(new Date());
			_tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		}
	}

	@Override
	public void finalize() throws Throwable {
		if (null != listener)
			listener = null;
		super.finalize();
	}

	private void commitTask(String tel_num, long talk_time, int talk_time_len) {
		CallInfo ci = userInfo.getCallInfo();
		if (null == ci || !tel_num.equals(ci.getTel_num())
				|| talk_time_len < ci.getTalk_time_len())
			return;
		// TODO
		Editor editor = preferences.edit();
		editor.putString("id", ci.getHandtask_id());
		editor.putInt("TALK_TIME_LEN", talk_time_len);
		editor.putLong("TALK_TIME", talk_time);
		editor.putString("TEL_NUM", tel_num);
		editor.commit();
	}

	private void findLast(Context ctx, String tel_num, long call_time) {
		Cursor _cursor = null;
		// TODO
		try {
			_cursor = ctx.getContentResolver().query(
					Calls.CONTENT_URI,
					porjection,
					Calls.TYPE + "=" + Calls.OUTGOING_TYPE + " AND "
							+ Calls.NUMBER + "=? AND " + Calls.DATE + ">"
							+ (call_time - 10 * 60 * 1000) + " AND 0<"
							+ Calls.DURATION, new String[] { tel_num },
					"DATE DESC LIMIT 1");
			// TODO
			if (_cursor.moveToFirst()) {
				Log.i(TAG,
						"===========" + _cursor.getString(0) + ","
								+ _cursor.getString(1) + ","
								+ _cursor.getString(2) + ","
								+ _cursor.getString(3));

				commitTask(_cursor.getString(1), _cursor.getLong(2),
						_cursor.getInt(3));
			} else {
				Log.i(TAG, "===========");
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (null != _cursor)
				_cursor.close();
		}
	}

	private class OnePhoneStateListener extends PhoneStateListener {
		private String tel_num;
		private Date call_time;
		private Context ctx;

		public OnePhoneStateListener(Context ctx) {
			this.ctx = ctx;
		}

		public void setTel_num(String tel_num) {
			this.tel_num = tel_num;
		}

		public void setCall_time(Date call_time) {
			this.call_time = call_time;
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: // 挂断
				findLast(ctx, tel_num, call_time.getTime());
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: // 接听
				break;
			case TelephonyManager.CALL_STATE_RINGING: // 响铃
				break;
			}
		}
	};
}
