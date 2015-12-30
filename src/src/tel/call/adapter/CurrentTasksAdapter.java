package tel.call.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tel.call.R;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class CurrentTasksAdapter extends BaseAdapter implements ListAdapter {

	private LayoutInflater inflater;
	private JSONArray data;
	private int resource;

	public CurrentTasksAdapter(int resource, Activity activity, JSONArray data) {
		this.resource = resource;
		this.data = data;
		inflater = LayoutInflater.from(activity);
	}

	@Override
	public int getCount() {
		return data.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			return data.get(position);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		// TODO
		if (null == convertView) {
			convertView = inflater.inflate(resource, null);
			// TODO
			viewHolder = new ViewHolder();
			// TODO
			viewHolder.no = (TextView) convertView.findViewById(R.id.no);
			viewHolder.task_name = (TextView) convertView
					.findViewById(R.id.task_name);
			viewHolder.issued_time = (TextView) convertView
					.findViewById(R.id.issued_time);
			viewHolder.status = (TextView) convertView
					.findViewById(R.id.status);
			// TODO
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// TODO
		try {
			Integer no = position + 1;
			// TODO
			JSONObject jo = data.getJSONObject(position);
			// TODO
			viewHolder.no.setText(no.toString());
			viewHolder.task_name.setText(jo.getString("TASK_NAME"));
			// viewHolder.issued_time.setText(DateUtil.getFormat1(jo
			// .getString("START_TIME")));
			viewHolder.issued_time.setText(jo.getString("TASK_SUM") + "/"
					+ jo.getString("SUCCESS_TASK_SUM"));
			// TODO
			viewHolder.status.setText("抢");
			viewHolder.status.setTextColor(Color.GREEN);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// TODO
		return convertView;
	}

	class ViewHolder {
		public TextView no;
		public TextView task_name;
		public TextView issued_time;
		public TextView status;
	}
}
