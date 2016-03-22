package tel.call.util;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import tel.call.R;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class HttpUtil implements Runnable {
	private final static String TAG = HttpUtil.class.getSimpleName();
	// TODO
	private Handler handler;
	private String url;
	private RequestMethod method;
	private HashMap<String, String> params;
	// TODO
	private Message msg;

	private JSONObject jo;

	/**
	 * 
	 * @param what
	 * @param handler
	 * @param url
	 * @param params
	 */
	public HttpUtil(int what, Handler handler, String url,
			RequestMethod method, HashMap<String, String> params) {
		this.handler = handler;
		this.url = url;
		this.method = method;
		this.params = params;
		// TODO
		msg = new Message();
		msg.what = what;
	}

	public HttpUtil(int what, Handler handler, String url,
			RequestMethod method, JSONObject jo) {
		this.handler = handler;
		this.url = url;
		this.method = method;
		this.jo = jo;
		// TODO
		msg = new Message();
		msg.what = what;
	}

	private static final int REQUEST_TIMEOUT = 5 * 1000; // 设置请求超时5秒钟
	private static final int SO_TIMEOUT = 5 * 1000; // 设置等待数据超时时间5秒钟

	private static HttpClient client;

	/**
	 * 请求超时时间和等待时间
	 * 
	 * @return
	 */
	@SuppressLint("UseValueOf")
	private static HttpClient getHttpClient() {
		if (null == client) {
			BasicHttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams,
					REQUEST_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
			client = new DefaultHttpClient(httpParams);
			client.getParams().setParameter("http.socket.timeout",
					new Integer(30 * 1000));
		}
		return client;
	}

	private static long TIME_OUT_IN_SECONDS = 120;

	/**
	 * 
	 */
	private void get(String params) {
		// TODO
		url += "?" + params;
		Log.i(TAG, url);
		// TODO
		HttpGet req = new HttpGet(url);
		// TODO
		try {
			HttpResponse res = getHttpClient().execute(req);
			if (HttpURLConnection.HTTP_OK == res.getStatusLine()
					.getStatusCode()) {
				HttpEntity entity = res.getEntity();
				String str = EntityUtils.toString(entity, "utf-8");
				// TODO
				Log.i(TAG, str);
				msg.obj = str;
			} else {
				msg.arg1 = R.string.valiate_network;
			}
		} catch (Exception e) {
			e.printStackTrace();
			msg.arg1 = R.string.valiate_network;
		}
		handler.sendMessage(msg);
	}

	/**
	 * 
	 */
	private void post(JSONObject jo) {
		long requestStratTime = new Date().getTime();

		// TODO
		Log.i(TAG, url);
		// TODO
		HttpPost req = new HttpPost(url);

		// TODO
		try {
			if (null != jo) {
				StringEntity entity = new StringEntity(jo.toString(), "utf-8");
				entity.setContentEncoding("UTF-8");
				entity.setContentType("application/json");
				req.setEntity(entity);
			}

			HttpResponse res = getHttpClient().execute(req);

			long requestEndTime = new Date().getTime();
			long timeOfRequest = (requestEndTime - requestStratTime) / 1000;
			if (null == res && timeOfRequest > TIME_OUT_IN_SECONDS) {
				msg.arg1 = R.string.valiate_network;
				return;
			}

			if (HttpURLConnection.HTTP_OK == res.getStatusLine()
					.getStatusCode()) {
				HttpEntity entity = res.getEntity();
				String str = EntityUtils.toString(entity, "utf-8");
				// TODO
				Log.i(TAG, str);
				msg.obj = str;
				return;
			}

			msg.arg1 = R.string.valiate_network;
		} catch (Exception e) {
			e.printStackTrace();
			msg.arg1 = R.string.valiate_network;
		} finally {
			req.abort();
			handler.sendMessage(msg);
		}
	}

	@Override
	public void run() {
		// TODO
		Iterator<Entry<String, String>> it = null;
		if (null != params) {
			it = params.entrySet().iterator();
		}
		// TODO
		switch (method) {
		case GET: {
			String params = "";
			// TODO
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				params += "&" + entry.getKey() + "=" + entry.getValue();
			}
			get(params.substring(0 == params.length() ? 0 : 1));
			break;
		}
		case POST: {
			post(jo);
			break;
		}
		default:
			break;
		}
	}

	public enum RequestMethod {
		POST, GET
	}
}
