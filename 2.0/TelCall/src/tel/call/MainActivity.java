package tel.call;

import java.net.URLEncoder;
import java.util.Date;

//import org.json.JSONArray;
//import tel.call.adapter.CurrentTasksAdapter;
//import java.util.HashMap;
//import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.json.JSONObject;

import tel.call.action.ServiceAction;
import tel.call.util.AppUtil;
import tel.call.util.HttpUtil;
import tel.call.util.HttpUtil.RequestMethod;
import tel.call.util.RestUtil;
import tel.call.util.UserInfo;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog.Calls;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;

//import android.widget.EditText;
//import android.widget.ListView;
//import android.os.AsyncTask;  

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class MainActivity extends ActionBarActivity {

	private final static String TAG = MainActivity.class.getSimpleName();

	// private Button btn_sync;
	// private ListView list_grid;

	private Button btn_get; // 抢单
	private Button btn_history;// 历史记录;

	private UserInfo userInfo;

	private AlertDialog.Builder dialog_alert;
	private AlertDialog.Builder dialog_exit;

	private SharedPreferences preferences;

	private Object[] os;
	private ProgressDialog proDialog;

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
		checkUnUploadData();
	}

	// @Override
	// protected void onResume() {
	// super.onResume();
	// Log.d(TAG, "-----start onResume-----");
	//
	// btn_get.setEnabled(false);
	// checkUnUploadData();
	// }

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ServiceAction.COMMIT_TASK:
				commitTask(msg);
				break;
				
			case  ServiceAction.UPLOAD_DATA:  //接收线程查找消息
				uploadData( msg) ;				
				break;

			// case ServiceAction.APPLY_TASK:
			// applyTask(msg);
			default:
				break;
			}
		}

		// 提交任务后反馈信息处理
		private void commitTask(Message msg) {
			btn_get.setEnabled(true);

			if (null == msg.obj) {
				showAlertDialog(getString(msg.arg1));
				return;
			}

			try {
				JSONObject _jo = new JSONObject((String) msg.obj);

				if (!_jo.getBoolean("success")) {
					showAlertDialog(null == _jo.getJSONArray("msg") ? "非法操作"
							: _jo.getJSONArray("msg").getString(0));
					return;
				}

				preferences = getSharedPreferences();

				// 提交任务完成后，删除任务ID
				Editor _editor = preferences.edit();
				_editor.remove("id");
				_editor.commit();

				showAlertDialog("已成功提交了一个任务!");
			} catch (JSONException e) {
				e.printStackTrace();
				showAlertDialog(e.getMessage());
			} finally {

			}
		}
	};

	private void showAlertDialog(String msg) {
		if (null == dialog_alert)
			dialog_alert = new AlertDialog.Builder(this);

		dialog_alert.setMessage(msg);
		dialog_alert.show();
	}

	// private void loadData()
	// {
	// setStatus_BtnSync(false);
	// setWidgetsStatus(false);
	//
	// checkUnUploadData();
	// }

	private void findView() {
		btn_get = (Button) findViewById(R.id.button1);
		btn_get.setEnabled(false);
		btn_history = (Button) findViewById(R.id.button2);

		dialog_exit = new AlertDialog.Builder(this);
		dialog_exit.setTitle("你确定要退出吗？");
		dialog_exit.setIcon(android.R.drawable.ic_dialog_info);
	}

	private SharedPreferences getSharedPreferences() {
		if (null == preferences)
			preferences = getSharedPreferences(AppUtil.UN_UPLOAD, MODE_PRIVATE);
		return preferences;
	}

	/**
	 * 检测未上传数据，无未上传记录则返回true
	 * 
	 */
	// 检查未上传记录 没有记录返回 真
	private boolean checkUnUploadData() {
		preferences = getSharedPreferences();

		final String id = preferences.getString("id", null);

		Log.d(TAG, "-----Check Task-----");
		if (null == id) {
			btn_get.setEnabled(true);
			return true;
		}

		if (!preferences.getBoolean("TASK_STATUS", false)) {
			btn_get.setEnabled(true);
			return true;
		}

		final int talk_time_len = preferences.getInt("TALK_TIME_LEN", 0);
		final long talk_time = preferences.getLong("START_TIME", 0);
		final String tel_num = preferences.getString("TEL_NUM", "");

		proDialog = android.app.ProgressDialog.show(MainActivity.this, "","正在查询,请稍等......");

		Thread thread = new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
					for (int i = 0; i < 5; i++) {
						os = findLast(id, tel_num, talk_time, talk_time_len);
						Thread.sleep(1000);

						if (null != os)
						{
							i = 5;
							
							Message msg = new Message();
							msg.what = ServiceAction.UPLOAD_DATA;
							
							Bundle bundle = new Bundle();							
							
							bundle.putString("id",id);						
							bundle.putString("telnum",tel_num);
							bundle.putLong("date", Long.parseLong( os[0].toString()));							
							bundle.putInt("timelen", 	Integer.parseInt( os[1].toString()));

							msg.setData(bundle);
							handler.sendMessage(msg);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				proDialog.dismiss();
			}
		};
		thread.start();

		// os = findLast(id, tel_num, talk_time, talk_time_len);

		if (null == os) {
			btn_get.setEnabled(true);
			return true;
		}

		//uploadData(id, tel_num, (Long) os[0], (Integer) os[1]);
		//btn_get.setEnabled(true);

		return false;
	}

	// 取得最后一次的通话记录
	private Object[] findLast(String id, String tel_num, long talk_time,	int talk_time_len) 
	{
		Cursor _cursor = null;

		try {
			_cursor = getContentResolver().query(
					Calls.CONTENT_URI,
					new String[] { Calls.TYPE, Calls.NUMBER, Calls.DATE,
							Calls.DURATION },
					Calls.TYPE + "=" + Calls.OUTGOING_TYPE + " AND "
							+ Calls.NUMBER + "=? AND " +
							// Calls.DATE + ">"+ (talk_time - 60 * 60 * 1000)
							Calls.DATE + ">" + talk_time
					// + " AND "+ talk_time_len + "<" + Calls.DURATION
					// //不检测通话时长，防止卡未满足时长的任务
					, new String[] { tel_num }, "DATE DESC LIMIT 1");

			if (_cursor.moveToFirst()) 
			{
				Log.d(TAG,						"/////////////////============" + _cursor.getString(0)
								+ "," + _cursor.getString(1) + ","
								+ _cursor.getString(2) + ","
								+ _cursor.getString(3)
								+ "==============//////////////");

				Object[] os = new Object[] { _cursor.getLong(2),	_cursor.getInt(3) };
				//Object[] os = new Object[] { id,tel_num ,_cursor.getLong(2),	_cursor.getInt(3) };
				
				return os;
			} else {
				Log.i(TAG, "===========");
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (null != _cursor)
				_cursor.close();
		}
		return null;
	}

	// 上传数据
//	private void uploadData(String id, String tel_num, long talk_time,	int talk_time_len) 
	private void uploadData(Message msg) 
	{
			Log.d(TAG,	 "--------------UPLOADDATA----------");
				
			String id="";
			String tel_num="";
			long talk_time=0;
			int talk_time_len=0;
			
			id = msg.getData().getString("id");
			tel_num = msg.getData().getString("telnum");
			talk_time = msg.getData().getLong("date");
			talk_time_len = msg.getData().getInt("timelen");		

		if (null == userInfo)
			userInfo = (UserInfo) getApplication();

		proDialog.setMessage("正在上传，请稍等......");

		JSONObject _jo = new JSONObject();

		try {
			_jo.put("command", "commitTask");
			long _ts = (new Date()).getTime() + userInfo.getTs();
			_jo.put("ts", Long.toString(_ts));

			_jo.put("apikey", userInfo.getApikey());

			JSONObject jo = new JSONObject();
			jo.put("TASKTAKE_ID", id);
			jo.put("TALK_TIME_LEN", talk_time_len);
			jo.put("TALK_TIME", talk_time);

			TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			jo.put("DEVICE_CODE", tm.getDeviceId());

			_jo.put("data", jo.toString());

			// Log.d(TAG,
			// "------------------------"+id+"     "+talk_time_len+"     "+talk_time+"     "+tel_num);

			String _paramStr = URLEncoder.encode(
					"apikey=" + userInfo.getApikey()
							+ "&command=commitTask&ts=" + _ts, "UTF-8");
			_jo.put("signature",RestUtil.standard(_paramStr, userInfo.getSeckey()));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		 
		Log.d(TAG, "-----UpLoad Task-----");

		HttpUtil _hu = new HttpUtil(ServiceAction.COMMIT_TASK, handler,	getString(R.string.httpUrl), RequestMethod.POST, _jo);
		Thread _t = new Thread(_hu);
		_t.start();

	}

	/**
	 * 事件绑定
	 */
	private void bind() {
		// 接单按钮点击事件
		btn_get.setOnClickListener(new OnClickListener() {
			// 向服务器提交抢单请求
			@Override
			public void onClick(View view) {
				try {
					// 在每次获取新任务时先检查未上传的任务,若有任务，等待上传完成后再审请新任务
					if (!checkUnUploadData())
						return;

					// Bundle _bundle = new Bundle();
					// _bundle.putString("TASK_ID","1" );
					// _bundle.putString("TEL_NUM","67101862");

					btn_get.setEnabled(false);

					Intent _intent = new Intent(MainActivity.this,
							DialActivity.class);
					// _intent.putExtras(_bundle);
					startActivity(_intent);
					// startActivityForResult(_intent,1);
				} catch (Exception e) {
					e.printStackTrace();
					showAlertDialog(e.getMessage());
				}
			}
		});

		// 历史记录按钮点击事件
		btn_history.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Uri _uri = Uri.parse(getString(R.string.UserUrl));
				Intent _intent = new Intent(Intent.ACTION_VIEW, _uri);
				startActivity(_intent);
			}
		});

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
			Uri _uri = Uri.parse(getString(R.string.UserUrl)
					+ "#page/changePwd");
			Intent _intent = new Intent(Intent.ACTION_VIEW, _uri);
			startActivity(_intent);
			break;
		}

		case R.id.action_taskHistory: {
			Uri _uri = Uri.parse(getString(R.string.UserUrl));
			Intent _intent = new Intent(Intent.ACTION_VIEW, _uri);
			startActivity(_intent);
			break;
		}

		case R.id.action_settings: {
			Uri _uri = Uri.parse(getString(R.string.UserUrl) + "#page/info");
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

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data)
	// {
	// if (0 == requestCode && RESULT_OK == resultCode)
	// {
	// setResult(RESULT_OK);
	// finish();
	// }
	//
	// if(1 == resultCode)
	// {
	// if (data != null)
	// {
	// Bundle bundle = data.getExtras();
	//
	// // Log.i( TAG,"===========!!!!!"
	// // + bundle.getString("result") );
	//
	// if (bundle != null && bundle.getString("result") =="ok" )// 得到子窗口的回传数据
	// {
	// Log.i( TAG," get  result == ok");
	// checkUnUploadData();
	// }
	// }
	// }
	// }

	@Override
	public void onBackPressed() {
		// if(!checkUnUploadData())return;

		if (null == dialog_exit) {
			dialog_exit = new AlertDialog.Builder(this);
		}
		dialog_exit.show();
	}
}