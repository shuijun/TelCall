package tel.call;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import tel.call.action.ServiceAction;
import tel.call.util.AppUtil;
import tel.call.util.HttpUtil;
import tel.call.util.HttpUtil.RequestMethod;
import tel.call.util.UserInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class LoginActivity extends Activity {

	private final static String TAG = LoginActivity.class.getSimpleName();

	private EditText text_username;
	private EditText text_userpass;
	private TextView link_register;
	private Button btn_showpass;
	private Button btn_login;

	private Toast toast;

	private AlertDialog.Builder dialog_alert;

	private void showToast(String msg) {
		if (null == toast)
			toast = Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_SHORT);
		else
			toast.setText(msg);
		toast.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_main);
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart() starting.");
		super.onStart();
		findView();
		bind();
	}

	private void findView() {
		text_username = (EditText) findViewById(R.id.text_username);
		text_userpass = (EditText) findViewById(R.id.text_userpass);
		link_register = (TextView) findViewById(R.id.link_register);
		btn_showpass = (Button) findViewById(R.id.btn_showpass);
		btn_login = (Button) findViewById(R.id.btn_login);
		// TODO
		text_username.setText(getCurrentMobileNum().replaceAll("\\+86", ""));

		dialog_alert = new AlertDialog.Builder(this);
		dialog_alert.setMessage("有新版本更新，请点击确定下载");
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ServiceAction.LOGIN:
				login(msg);
				break;
			default:
				break;
			}
		}

		/**
		 * 
		 * @param msg
		 */
		private void login(Message msg) {
			// TODO
			if (null == msg.obj) {
				showToast(getString(msg.arg1));
				setBtnLoginStatus(true);
				return;
			}

			// TODO
			try {
				// TODO
				JSONObject _jo = new JSONObject((String) msg.obj);

				// TODO 版本检测
				if (AppUtil.getVerCode(LoginActivity.this) < _jo.getInt("ver")) {
					dialog_alert.show();
					setBtnLoginStatus(true);
					return;
				}

				// TODO
				if (!_jo.getBoolean("success")) {
					showToast(_jo.getJSONArray("msg").getString(0));
					setBtnLoginStatus(true);
					return;
				}
				// TODO
				JSONObject _jdata = _jo.getJSONObject("data");
				// TODO
				UserInfo app = (UserInfo) getApplication();
				app.setApikey(_jdata.getString("APIKEY"));
				app.setSeckey(_jdata.getString("SECKEY"));
				app.setTs(_jdata.getLong("TS"));
			} catch (JSONException e) {
				e.printStackTrace();
				showToast(e.getMessage());
				setBtnLoginStatus(true);
				return;
			}
			// TODO
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			// TODO
			startActivity(intent);
			finish();
		}
	};

	private void setBtnLoginStatus(boolean status) {
		btn_login.setEnabled(status);
		btn_login.setText(status ? R.string.login_main_btn_login
				: R.string.login_main_btn_login_ing);
	}

	private void login(final String user_name, final String user_pass) {
		// TODO
		HashMap<String, String> _params = new HashMap<String, String>();
		// TODO
		_params.put("command", "login");
		long ts = (new Date()).getTime();
		_params.put("ts", Long.toString(ts));

		// TODO
		try {
			JSONObject jo = new JSONObject();
			jo.put("USER_NAME", user_name);
			jo.put("USER_PASS", user_pass);
			// TODO
			_params.put("data", URLEncoder.encode(jo.toString(), "utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
			showToast(e.getMessage());
			setBtnLoginStatus(true);
			return;
		}

		// TODO
		HttpUtil _hu = new HttpUtil(ServiceAction.LOGIN, handler,
				getString(R.string.httpUrl) + "api", RequestMethod.POST,
				_params);
		Thread _t = new Thread(_hu);
		_t.start();
	}

	/**
	 * 获取当前手机号
	 * 
	 * @return
	 */
	public String getCurrentMobileNum() {
		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
	}

	private void bind() {
		// TODO
		dialog_alert.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						Uri uri = Uri.parse(getString(R.string.AppUrl));
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				});

		// click
		btn_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setBtnLoginStatus(false);
				// TODO
				String _user_name = text_username.getText().toString().trim();
				String _user_pass = text_userpass.getText().toString().trim();
				// TODO
				if ("".equals(_user_name)) {
					showToast(getString(R.string.valiate_userpass));
					setBtnLoginStatus(true);
					return;
				}
				if ("".equals(_user_pass)) {
					showToast(getString(R.string.valiate_userpass));
					setBtnLoginStatus(true);
					return;
				}
				// TODO
				login(_user_name, _user_pass);
			}
		});

		// click
		btn_showpass.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (getString(R.string.login_main_btn_show).equals(
						btn_showpass.getText())) {
					text_userpass
							.setTransformationMethod(HideReturnsTransformationMethod
									.getInstance());
					btn_showpass.setText(R.string.login_main_btn_hide);
				} else {
					text_userpass
							.setTransformationMethod(PasswordTransformationMethod
									.getInstance());
					btn_showpass.setText(R.string.login_main_btn_show);
				}
			}
		});

		// click
		link_register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Uri uri = Uri.parse(getString(R.string.httpUrl)
						+ "user/register");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});

		// timer
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				popKeyboard();
			}

			/**
			 * 弹出软键盘
			 */
			private void popKeyboard() {
				InputMethodManager manager = (InputMethodManager) text_username
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				manager.showSoftInput(text_username, 0);
			}
		}, 1000 * 1);
	}
}