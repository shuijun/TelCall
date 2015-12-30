package tel.call.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tel.call.model.Task;
import tel.call.util.DateUtil;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class DBManager {
	private final static String TAG = "DBManager";

	private DBHelper helper;
	private SQLiteDatabase db;
	// TODO
	private static final String queryTasks_sql = "SELECT * FROM p_task ORDER BY ISSUED_TIME DESC";

	public DBManager(Context context) {
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
		initData();
	}

	private void initData() {
		boolean b = false;
		if (b)
			initData_task();
	}

	private void initData_task() {
		ContentValues values = null;
		// TODO
		values = new ContentValues();
		values.put("id", "b100323407df40b69d632aa8cdf8f876");
		values.put("TASK_NAME", "1");
		values.put("TASK_CONTENT", "2");
		values.put("ISSUED_TIME", DateUtil.getFormat2());
		values.put("STATUS", "4");
		values.put("CREATE_TIME", DateUtil.getFormat2());
		db.insert("p_task", null, values);
		// TODO
		values = new ContentValues();
		values.put("id", "e77741925ef241c09b68342f54b9f779");
		values.put("TASK_NAME", "11");
		values.put("TASK_CONTENT", "22");
		values.put("ISSUED_TIME", DateUtil.getFormat2());
		values.put("STATUS", "44");
		values.put("CREATE_TIME", DateUtil.getFormat2());
		db.insert("p_task", null, values);
		// TODO
		values = new ContentValues();
		values.put("id", "5e32a2c5ed634e7daf8f1049ce926e70");
		values.put("TASK_NAME", "111");
		values.put("TASK_CONTENT", "222");
		values.put("ISSUED_TIME", DateUtil.getFormat2());
		values.put("STATUS", "444");
		values.put("CREATE_TIME", DateUtil.getFormat2());
		db.insert("p_task", null, values);
	}

	public List<Task> queryTasks() {
		ArrayList<Task> tasks = new ArrayList<Task>();
		Cursor cursor = db.rawQuery(queryTasks_sql, null);
		while (cursor.moveToNext()) {
			Task task = new Task();
			// TODO
			task.setId(cursor.getString(cursor.getColumnIndex("id")));
			task.setTask_name(cursor.getString(cursor
					.getColumnIndex("TASK_NAME")));
			task.setTask_content(cursor.getString(cursor
					.getColumnIndex("TASK_CONTENT")));
			task.setIssued_time(new Date());
			task.setStatus(cursor.getInt(cursor.getColumnIndex("STATUS")));
			task.setCreate_time(new Date());
			// TODO
			tasks.add(task);
		}
		cursor.close();
		return tasks;
	}

	/**
	 * 添加任务
	 * 
	 * @param task
	 */
	public void addTask(Task task) {
		if (null == db)
			return;
		db.beginTransaction();
		try {
			db.execSQL("INSERT INTO p_task VALUES(?, ?, ?, ?, ?, ?)",
					new Object[] { task.getId() });
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			db.endTransaction();
		}
	}

	public void close() {
		if (null != db)
			db.close();
	}
}
