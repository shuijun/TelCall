package tel.call;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tel.call.action.ServiceAction;
import tel.call.adapter.CurrentTasksAdapter;
import tel.call.util.AppUtil;
import tel.call.util.DateUtil;
import tel.call.util.HttpUtil;
import tel.call.util.HttpUtil.RequestMethod;
import tel.call.util.RestUtil;
import tel.call.util.UserInfo;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class MainActivity extends ActionBarActivity {

	private final static String TAG = MainActivity.class.getSimpleName();

	// TODO
	private Button btn_sync;
	private ListView list_grid;
	private EditText text_sel_date;

	// TODO
	private UserInfo userInfo;

	private AlertDialog.Builder dialog_alert;
	private AlertDialog.Builder dialog_exit;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (checkLogin())
			return;
		Log.d(TAG, "onCreate() starting.");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		// TODO
		// dbMgr = new DBManager(this);
	}

	@Override
	protected void onDestroy() {
		// if (null != dbMgr) dbMgr.close();
		super.onDestroy();
	}

	/**
	 * 检测登陆状态，否则并跳转
	 */
	private boolean checkLogin() {
		return false;
	}

	@Override
	public void onStart() {
		super.onStart();
		findView();
		bind();
		loadData();
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ServiceAction.GET_CURRENTTASKS:
				getCurrentTasks(msg);
				break;
			case ServiceAction.COMMIT_TASK:
				commitTask(msg);
				break;
			default:
				break;
			}
		}

		private void commitTask(Message msg) {
			// TODO
			if (null == msg.obj) {
				showAlertDialog(getString(msg.arg1));
				setStatus_BtnSync(true);
				setWidgetsStatus(true);
				return;
			}

			// TODO
			try {
				JSONObject _jo = new JSONObject((String) msg.obj);

				// TODO
				if (!_jo.getBoolean("success")) {
					showAlertDialog(_jo.getJSONArray("msg").getString(0));
					return;
				}
				// TODO
				if (null == preferences)
					preferences = getSharedPreferences(AppUtil.UN_UPLOAD,
							MODE_PRIVATE);

				// TODO
				Editor _editor = preferences.edit();
				_editor.remove("id");
				_editor.commit();
			} catch (JSONException e) {
				e.printStackTrace();
				showAlertDialog(e.getMessage());
			} finally {
				setStatus_BtnSync(true);
				setWidgetsStatus(true);
			}
		}

		private void getCurrentTasks(Message msg) {
			// TODO
			if (null == msg.obj) {
				showAlertDialog(getString(msg.arg1));
				setStatus_BtnSync(true);
				setWidgetsStatus(true);
				return;
			}

			// TODO
			try {
				JSONObject _jo = new JSONObject((String) msg.obj);
				// TODO
				if (!_jo.getBoolean("success")) {
					showAlertDialog(_jo.getJSONArray("msg").getString(0));
					return;
				}
				// TODO
				JSONArray _jdata = _jo.getJSONArray("data");
				// TODO
				CurrentTasksAdapter _adapter = new CurrentTasksAdapter(
						R.layout.fragment_main_datagrid, MainActivity.this,
						_jdata);
				list_grid.setAdapter(_adapter);
			} catch (JSONException e) {
				e.printStackTrace();
				showAlertDialog(e.getMessage());
			} finally {
				setStatus_BtnSync(true);
				setWidgetsStatus(true);
			}
		}
	};

	/**
	 * 获取当前可接的任务
	 */
	private void loadData_grid() {
		if (null == userInfo)
			userInfo = (UserInfo) getApplication();

		// TODO
		HashMap<String, String> _params = new HashMap<String, String>();
		// TODO
		_params.put("command", "getCurrentTasks");
		long _ts = (new Date()).getTime() + userInfo.getTs();
		_params.put("ts", Long.toString(_ts));

		// TODO
		try {
			_params.put("apikey",
					URLEncoder.encode(userInfo.getApikey(), "UTF-8"));

			JSONObject _jo = new JSONObject();
			String _data = _jo.toString();
			_params.put("data", URLEncoder.encode(_data, "UTF-8"));
			// TODO
			String _paramStr = URLEncoder.encode(
					"apikey=" + userInfo.getApikey()
							+ "&command=getCurrentTasks&data=" + _data + "&ts="
							+ _ts, "UTF-8");
			_params.put("signature",
					RestUtil.standard(_paramStr, userInfo.getSeckey()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			showAlertDialog(e.getMessage());
			setStatus_BtnSync(true);
			setWidgetsStatus(true);
			return;
		}

		// TODO
		HttpUtil _hu = new HttpUtil(ServiceAction.GET_CURRENTTASKS, handler,
				getString(R.string.httpUrl) + "api", RequestMethod.POST,
				_params);
		Thread _t = new Thread(_hu);
		_t.start();
	}

	/**
	 * 上传数据
	 * 
	 * @param id
	 * @param tel_num
	 * @param talk_time
	 * @param talk_time_len
	 */
	private void uploadData(String id, String tel_num, long talk_time,
			int talk_time_len) {
		if (null == userInfo)
			userInfo = (UserInfo) getApplication();

		// TODO
		HashMap<String, String> _params = new HashMap<String, String>();
		// TODO
		_params.put("command", "commitTask");
		long _ts = (new Date()).getTime() + userInfo.getTs();
		_params.put("ts", Long.toString(_ts));

		// TODO
		try {
			_params.put("apikey",
					URLEncoder.encode(userInfo.getApikey(), "UTF-8"));

			JSONObject _jo = new JSONObject();
			_jo.put("id", id);
			_jo.put("TALK_TIME_LEN", talk_time_len);
			_jo.put("TALK_TIME", talk_time);
			_jo.put("TEL_NUM", tel_num);

			// TODO
			String _data = _jo.toString();
			_params.put("data", URLEncoder.encode(_data, "UTF-8"));

			String _paramStr = URLEncoder.encode(
					"apikey=" + userInfo.getApikey()
							+ "&command=commitTask&data=" + _data + "&ts="
							+ _ts, "UTF-8");
			_params.put("signature",
					RestUtil.standard(_paramStr, userInfo.getSeckey()));
			// TODO
		} catch (Exception e) {
			e.printStackTrace();
			showAlertDialog(e.getMessage());
			setStatus_BtnSync(true);
			setWidgetsStatus(true);
			return;
		}

		// TODO
		HttpUtil _hu = new HttpUtil(ServiceAction.COMMIT_TASK, handler,
				getString(R.string.httpUrl) + "api", RequestMethod.POST,
				_params);
		Thread _t = new Thread(_hu);
		_t.start();
	}

	private void showAlertDialog(String msg) {
		if (null == dialog_alert)
			dialog_alert = new AlertDialog.Builder(this);

		// TODO
		dialog_alert.setMessage(msg);
		dialog_alert.show();
	}

	private void loadData() {
		setStatus_BtnSync(false);
		setWidgetsStatus(false);
		// TODO
		if (checkUnUploadData())
			loadData_grid();
	}

	private void findView() {
		btn_sync = (Button) findViewById(R.id.btn_sync);
		list_grid = (ListView) findViewById(R.id.grid_items);

		// TODO
		text_sel_date = (EditText) findViewById(R.id.text_sel_date);
		text_sel_date.setText(DateUtil.getFormat2());
		text_sel_date.setEnabled(false);

		// TODO
		dialog_exit = new AlertDialog.Builder(this);
		dialog_exit.setTitle("你确定要退出吗？");
		dialog_exit.setIcon(android.R.drawable.ic_dialog_info);
	}

	private void setStatus_BtnSync(boolean status) {
		btn_sync.setText(status ? "刷新" : "正在刷新");
	}

	private void setWidgetsStatus(boolean status) {
		btn_sync.setEnabled(status);
		list_grid.setEnabled(status);
	}

	/**
	 * 检测未上传数据，无未上传记录则返回true
	 * 
	 * @return
	 */
	private boolean checkUnUploadData() {
		if (null == preferences)
			preferences = getSharedPreferences(AppUtil.UN_UPLOAD, MODE_PRIVATE);

		// TODO
		String id = preferences.getString("id", null);
		if (null == id)
			return true;

		int talk_time_len = preferences.getInt("TALK_TIME_LEN", 0);
		long talk_time = preferences.getLong("TALK_TIME", 0);
		String tel_num = preferences.getString("TEL_NUM", "");

		uploadData(id, tel_num, talk_time, talk_time_len);
		return false;
	}

	/**
	 * 事件绑定
	 */
	private void bind() {
		// click
		btn_sync.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				loadData();
			}
		});

		// TODO
		list_grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int position,
					long id) {
				if (!checkUnUploadData())
					return;

				setWidgetsStatus(false);

				// TODO
				JSONObject _jo = (JSONObject) list_grid
						.getItemAtPosition(position);
				// TODO
				try {
					// TODO
					Bundle _bundle = new Bundle();
					_bundle.putString("TASK_ID", _jo.getString("id"));
					_bundle.putString("TEL_NUM", _jo.getString("TEL_NUM"));
					Intent _intent = new Intent(MainActivity.this,
							DialActivity.class);
					_intent.putExtras(_bundle);
					startActivity(_intent);
				} catch (JSONException e) {
					e.printStackTrace();
					showAlertDialog(e.getMessage());
					setWidgetsStatus(true);
				}
			}
		});

		// TODO
		dialog_exit.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						MainActivity.this.finish();
					}
				});
		dialog_exit.setNegativeButton("返回",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_changePwd: {
			Uri _uri = Uri.parse(getString(R.string.httpUrl) + "u/changePwd");
			Intent _intent = new Intent(Intent.ACTION_VIEW, _uri);
			startActivity(_intent);
			break;
		}
		case R.id.action_taskHistory: {
			Uri _uri = Uri.parse(getString(R.string.httpUrl) + "u/task/");
			Intent _intent = new Intent(Intent.ACTION_VIEW, _uri);
			startActivity(_intent);
			break;
		}
		case R.id.action_settings: {
			Uri _uri = Uri.parse(getString(R.string.httpUrl) + "u/");
			Intent _intent = new Intent(Intent.ACTION_VIEW, _uri);
			startActivity(_intent);
			break;
		}
		default:
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
			// TODO
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (0 == requestCode && RESULT_OK == resultCode) {
			setResult(RESULT_OK);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		if (null == dialog_exit) {
			dialog_exit = new AlertDialog.Builder(this);
		}
		dialog_exit.show();
	}
}