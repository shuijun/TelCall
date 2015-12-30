package tel.call;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class SplashActivity extends Activity {
	private static final String TAG = SplashActivity.class.getSimpleName();

	private static final int GO_GUIDE = 101;
	private static final int GO_MAIN = 102;
	private static final long SPLASH_DELAY_MILLIS = 1000 * 1;

	private static final String PREFERENCE_NAME = "splash_activity";

	private ImageView img_welcome;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_main);
	}

	@Override
	public void onStart() {
		super.onStart();
		findView();
		init();
	}

	private void findView() {
		img_welcome = (ImageView) findViewById(R.id.welcome_img);
		// 默认隐藏
		img_welcome.setVisibility(View.INVISIBLE);
		img_welcome.setVisibility(View.VISIBLE);
	}

	private void init() {
		Log.d(TAG, "init() starting.");
		// TODO
		if (isFirstUse()) {
			handler.sendEmptyMessageDelayed(GO_GUIDE, SPLASH_DELAY_MILLIS);
			return;
		}
		// TODO
		handler.sendEmptyMessageDelayed(GO_MAIN, SPLASH_DELAY_MILLIS);
	}

	/**
	 * 首次使用
	 * 
	 * @return
	 */
	private boolean isFirstUse() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);
		return preferences.getBoolean("isFirstUse", true);
	}

	/**
	 * 改变首次使用状态
	 */
	private void setFirstUse() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean("isFirstUse", false);
		editor.commit();
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_GUIDE:
				goGuide();
				break;
			case GO_MAIN:
				goMain();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void goGuide() {
		// TODO
		setFirstUse();
		goMain();
	}

	private void goMain() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}
}