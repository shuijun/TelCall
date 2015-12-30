package tel.call.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class DBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "tel.hyx";
	private static final int DB_VER = 1;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		create_task(db);
	}

	private void create_task(SQLiteDatabase db) {
		String sql = "CREATE TABLE [p_task] ("
				+ "[id] VARCHAR(32), [TASK_NAME] VARCHAR(128), [TASK_CONTENT] VARCHAR(512), [ISSUED_TIME] DATETIME, [STATUS] INT(2), [CREATE_TIME] DATETIME)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
		db.execSQL("DROP TABLE IF EXISTS 'p_task';");
		onCreate(db);
	}
}
