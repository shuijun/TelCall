package tel.call.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class AppUtil {
	public static final String UN_UPLOAD = "UN_UPLOAD";

	public static int getVerCode(Context context) {
		int verCode = 0;
		try {
			verCode = context.getPackageManager().getPackageInfo("tel.call", 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return verCode;
	}
}
