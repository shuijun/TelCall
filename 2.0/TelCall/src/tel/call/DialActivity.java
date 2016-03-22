package tel.call;

import java.net.URLEncoder;
import java.util.Date;

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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.telephony.TelephonyManager;
import android.util.Log;

//import android.database.Cursor;
//import android.provider.CallLog.Calls;

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
	// private Button btn_upload;
	private UserInfo userInfo;

	private AlertDialog.Builder dialog_alert;
	private SharedPreferences preferences;

	@Override
	// 创建页面
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dial_main);

		userInfo = (UserInfo) getApplication();
	}

	@Override
	public void onStart() {
		// Log.d(TAG, "onStart() starting.");
		super.onStart();
		findView();
		bind();
		loadData();
	}

	// 页面刷新
	// @Override
	// protected void onResume()
	// {
	// super.onResume();
	//
	// if(!checkUnUploadData())//显示上传数据据钮
	// {
	// btn_dial.setVisibility(View.GONE);
	// btn_upload.setVisibility(View.VISIBLE);
	// }
	// else
	// btn_upload.setVisibility(View.INVISIBLE);
	//
	// }
	//
	// @Override
	// protected void onPause() {
	// super.onPause();
	// Log.e(TAG, "~~~start onPause~~~");
	// }
	// @Override
	// protected void onStop() {
	// super.onStop();
	// Log.e(TAG, "~~~start onStop~~~");
	// }
	//
	// @Override
	// protected void onRestart()
	// {
	// Log.e(TAG, "~~~start onRestart~~~");
	// super.onRestart();
	// }

	private void loadData() {
		applyTask();
	}

	// 获取本地数据存储单元
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

		// 消息处理，接收服务器的任务申请
		@SuppressLint("CommitPrefEdits")
		private void applyTask(Message msg) {
			if (null == msg.obj) {
				showAlertDialog(getString(msg.arg1));
				return;
			}

			preferences = getSharedPreferences();
			Editor _editor = preferences.edit();

			try {
				JSONObject _jo = new JSONObject((String) msg.obj);

				// 申请任务返回信息 不成功
				if (!_jo.getBoolean("success")) {
					showAlertDialog(null == _jo.getJSONArray("msg") ? "非法操作"
							: _jo.getJSONArray("msg").getString(0));
					// DialActivity.this.finish();
					return;
				}

				JSONObject _jdata = _jo.getJSONObject("data");

				// STATUS 大于 0，该任务不可用，关闭页面
				// if(_jdata.getInt("STATUS")>0)
				// {
				// DialActivity.this.finish();
				// return;
				// }

				text_task_name.setText("任务名称：" + _jdata.getString("TASK_NAME"));
				text_tel_num.setText("电话号码：" + _jdata.getString("TEL_NUM"));
				text_talk_timeout.setText("任务过期："
						+ DateUtil.getFormat4(
								_jdata.getString("TASKTAKE_CREATE_TIME"),
								_jdata.getInt("TALK_TIMEOUT")));
				text_talk_time_len.setText("通话时长：不能少于 "
						+ _jdata.getString("TALK_TIME_MIN") + " 秒");

				// 短信内容
				text_sms_intro.setText(_jdata.getString("SMS_INTRO"));
				// 提示信息
				text_task_intro.setText(_jdata.getString("TASK_INTRO"));

				// 存储任务信息至本机存储单元
				_editor.putString("id", _jdata.getString("TASKTAKE_ID"));
				_editor.putLong("START_TIME",
						DateUtil.getFormat5(_jdata.getString("START_TIME")));
				_editor.putInt("TALK_TIME_MIN", _jdata.getInt("TALK_TIME_MIN"));
				_editor.putString("TEL_NUM", _jdata.getString("TEL_NUM"));
				_editor.putBoolean("TASK_STATUS", false);

				_editor.commit();

				btn_dial.setEnabled(true);

			} catch (JSONException e) {
				e.printStackTrace();

				_editor.remove("id");
				_editor.commit();
				showAlertDialog(e.getMessage());
			}
		}
	};

	// 向服务器申请 取得一个任务的详细信息
	// private void applyTask(String task_id) {
	private void applyTask() {
		// HashMap<String, String> _params = new HashMap<String, String>();

		// _params.put("command", "applyTask");
		// long _ts = (new Date()).getTime() + userInfo.getTs();
		// _params.put("ts", Long.toString(_ts));

		Log.d(TAG, "====APPLY TASK======");

		JSONObject _jo = new JSONObject();

		try {
			_jo.put("command", "applyTask");

			long _ts = (new Date()).getTime() + userInfo.getTs();

			_jo.put("ts", Long.toString(_ts));
			_jo.put("apikey", userInfo.getApikey());

			JSONObject jo = new JSONObject();

			TelephonyManager tm = (TelephonyManager) this
					.getSystemService(Context.TELEPHONY_SERVICE);
			jo.put("DEVICE_CODE", tm.getDeviceId());

			_jo.put("data", jo.toString());

			String _paramStr = URLEncoder.encode(
					"apikey=" + userInfo.getApikey() + "&command=applyTask&ts="
							+ _ts, "UTF-8");
			_jo.put("signature",
					RestUtil.standard(_paramStr, userInfo.getSeckey()));
		} catch (Exception e) {
			e.printStackTrace();
			showAlertDialog(e.getMessage());
			return;
		}

		HttpUtil _hu = new HttpUtil(ServiceAction.APPLY_TASK, handler,
				getString(R.string.httpUrl), RequestMethod.POST, _jo);
		Thread _t = new Thread(_hu);
		_t.start();
	}

	private void showAlertDialog(String msg) {
		if ("".equals(msg))
			msg = "点击确定返回！";

		dialog_alert.setMessage(msg);
		dialog_alert.show();
	}

	// 页面UI
	private void findView() {
		text_task_name = (EditText) findViewById(R.id.text_task_name);
		text_tel_num = (EditText) findViewById(R.id.text_tel_num);
		text_task_intro = (EditText) findViewById(R.id.text_task_intro);
		text_talk_timeout = (EditText) findViewById(R.id.text_talk_timeout);
		text_talk_time_len = (EditText) findViewById(R.id.text_talk_time_len);
		text_sms_intro = (EditText) findViewById(R.id.text_sms_intro);

		btn_dial = (Button) findViewById(R.id.btn_dial);
		// btn_upload = (Button) findViewById(R.id.btn_upload);

		dialog_alert = new AlertDialog.Builder(this);
	}

	private void bind() {
		// click
		btn_dial.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				preferences = getSharedPreferences();

				Editor _editor = preferences.edit();
				_editor.putLong("TALK_TIME", (new Date()).getTime());
				_editor.putBoolean("TASK_STATUS", true);
				_editor.commit();

				// Bundle _bundle = getIntent().getExtras();

				// 用intent启动拨打电话 没有TEL_NUM 缺省返回10000号
				Intent _intent = new Intent(Intent.ACTION_CALL, Uri
						.parse("tel:"
								+ preferences.getString("TEL_NUM", "10000")));
				DialActivity.this.startActivity(_intent);

				DialActivity.this.finish();
			}
		});

		// btn_upload.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View view)
		// {
		// btn_upload.setEnabled(false);
		//
		// String id = preferences.getString("id", null);
		// String tel_num = preferences.getString("TEL_NUM", "");
		// uploadData(id, tel_num, (Long) os[0], (Integer) os[1]);
		//
		// //传结果至上层页面
		// Intent intent = new Intent();
		// intent.putExtra("result", "ok");// 把返回数据存入Intent
		// setResult(1, intent);//
		//
		// DialActivity.this.finish();
		// }
		// });

		dialog_alert.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						// 传结果至上层页面
						// Intent intent = new Intent();
						// intent.putExtra("result", "fail");// 把返回数据存入Intent
						// setResult(1, intent);//

						DialActivity.this.finish();
					}
				});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && 0 == event.getRepeatCount()) {
			// 传结果至上层页面--
			// Intent intent = new Intent();
			// intent.putExtra("result", "back");// 把返回数据存入Intent
			// setResult(1, intent);//

			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
