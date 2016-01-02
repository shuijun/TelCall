package tel.call;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import tel.call.action.ServiceAction;
import tel.call.util.AppUtil;
import tel.call.util.DateUtil;
import tel.call.util.HttpUtil;
import tel.call.util.HttpUtil.RequestMethod;
import tel.call.util.RestUtil;
import tel.call.util.UserInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class DialActivity extends Activity {

	private final static String TAG = DialActivity.class.getSimpleName();

	private EditText text_task_name;
	private EditText text_tel_num;
	private EditText text_talk_timeout;
	private EditText text_task_intro;
	private EditText text_talk_time_len;
	private EditText text_sms_intro;

	private Button btn_dial;
	private UserInfo userInfo;

	private AlertDialog.Builder dialog_alert;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dial_main);
		// TODO
		userInfo = (UserInfo) getApplication();
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart() starting.");
		super.onStart();
		findView();
		bind();
		loadData();
	}

	private void loadData() {
		Bundle _bundle = getIntent().getExtras();
		applyTask(_bundle.getString("TASK_ID"));
	}

	private SharedPreferences getSharedPreferences() {
		if (null == preferences)
			preferences = getSharedPreferences(AppUtil.UN_UPLOAD, MODE_PRIVATE);
		return preferences;
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ServiceAction.APPLY_TASK:
				applyTask(msg);
				break;
			default:
				break;
			}
		}

		@SuppressLint("CommitPrefEdits")
		private void applyTask(Message msg) {
			// TODO
			if (null == msg.obj) {
				showAlertDialog(getString(msg.arg1));
				return;
			}

			// TODO
			preferences = getSharedPreferences();
			Editor _editor = preferences.edit();

			// TODO
			try {
				JSONObject _jo = new JSONObject((String) msg.obj);
				// TODO
				if (!_jo.getBoolean("success")) {
					showAlertDialog(_jo.getJSONArray("msg").getString(0));
					return;
				}

				// TODO
				JSONObject _jdata = _jo.getJSONObject("data");

				// TODO
				text_task_name.setText("任务名称：" + _jdata.getString("TASK_NAME"));
				text_tel_num.setText("电话号码：" + _jdata.getString("TEL_NUM"));
				text_task_intro.setText(_jdata.getString("TASK_INTRO"));
				text_talk_timeout.setText("任务过期："
						+ DateUtil.getFormat4(
								_jdata.getString("HANDTASK_CREATE_TIME"),
								_jdata.getInt("TALK_TIMEOUT")));
				text_talk_time_len.setText("通话时长：不能少于 "
						+ _jdata.getString("TALK_TIME_LEN") + " 秒");
				text_sms_intro.setText(_jdata.getString("SMS_INTRO"));

				// TODO
				_editor.putString("id", _jdata.getString("HANDTASK_ID"));
				_editor.putInt("TALK_TIME_LEN", _jdata.getInt("TALK_TIME_LEN"));
				_editor.putString("TEL_NUM", _jdata.getString("TEL_NUM"));
				_editor.commit();
				// TODO
				btn_dial.setEnabled(true);
			} catch (JSONException e) {
				e.printStackTrace();
				// TODO
				_editor.remove("id");
				_editor.commit();
				showAlertDialog(e.getMessage());
			}
		}
	};

	private void applyTask(String task_id) {
		HashMap<String, String> _params = new HashMap<String, String>();
		// TODO
		_params.put("command", "applyTask");
		long _ts = (new Date()).getTime() + userInfo.getTs();
		_params.put("ts", Long.toString(_ts));

		// TODO
		try {
			_params.put("apikey",
					URLEncoder.encode(userInfo.getApikey(), "UTF-8"));

			JSONObject _jo = new JSONObject();
			_jo.put("TASK_ID", task_id);
			String data = _jo.toString();
			_params.put("data", URLEncoder.encode(data, "UTF-8"));

			String _paramStr = URLEncoder.encode(
					"apikey=" + userInfo.getApikey()
							+ "&command=applyTask&data=" + data + "&ts=" + _ts,
					"UTF-8");
			_params.put("signature",
					RestUtil.standard(_paramStr, userInfo.getSeckey()));
		} catch (Exception e) {
			e.printStackTrace();
			showAlertDialog(e.getMessage());
			return;
		}

		// TODO
		HttpUtil _hu = new HttpUtil(ServiceAction.APPLY_TASK, handler,
				getString(R.string.httpUrl) + "api", RequestMethod.POST,
				_params);
		Thread _t = new Thread(_hu);
		_t.start();
	}

	private void showAlertDialog(String msg) {
		// TODO
		dialog_alert.setMessage(msg);
		dialog_alert.show();
	}

	private void findView() {
		// TODO
		text_task_name = (EditText) findViewById(R.id.text_task_name);
		text_tel_num = (EditText) findViewById(R.id.text_tel_num);
		text_task_intro = (EditText) findViewById(R.id.text_task_intro);
		text_talk_timeout = (EditText) findViewById(R.id.text_talk_timeout);
		text_talk_time_len = (EditText) findViewById(R.id.text_talk_time_len);
		text_sms_intro = (EditText) findViewById(R.id.text_sms_intro);
		// TODO
		btn_dial = (Button) findViewById(R.id.btn_dial);
		// TODO
		dialog_alert = new AlertDialog.Builder(this);
	}

	private void bind() {
		// click
		btn_dial.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO
				preferences = getSharedPreferences();
				Editor _editor = preferences.edit();
				_editor.putLong("TALK_TIME", (new Date()).getTime());
				_editor.commit();
				// TODO
				Bundle _bundle = getIntent().getExtras();
				// 用intent启动拨打电话
				Intent _intent = new Intent(Intent.ACTION_CALL, Uri
						.parse("tel:" + _bundle.getString("TEL_NUM")));
				DialActivity.this.startActivity(_intent);
			}
		});

		// TODO
		dialog_alert.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						DialActivity.this.finish();
					}
				});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && 0 == event.getRepeatCount()) {
			// TODO
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
