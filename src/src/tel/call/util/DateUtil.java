package tel.call.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
@SuppressLint("SimpleDateFormat")
public class DateUtil {

	private static final SimpleDateFormat sdf_1 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static final SimpleDateFormat sdf_2 = new SimpleDateFormat(
			"MM/dd/HH");
	private static final SimpleDateFormat sdf_3 = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static final SimpleDateFormat sdf_4 = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String getFormat1(String date) {
		sdf_1.setTimeZone(TimeZone.getTimeZone("UTC"));
		// TODO
		try {
			Date _date = sdf_1.parse(date);
			return sdf_2.format(_date);
		} catch (ParseException e) {
			return "";
		}
	}

	/**
	 * 
	 * @return
	 */
	public static String getFormat2() {
		return sdf_3.format(new Date());
	}

	public static String getFormat3(String date) {
		sdf_1.setTimeZone(TimeZone.getTimeZone("UTC"));
		// TODO
		try {
			Date _date = sdf_1.parse(date);
			return sdf_4.format(_date);
		} catch (ParseException e) {
			return "";
		}
	}

	public static String getFormat4(String date, int second) {
		sdf_1.setTimeZone(TimeZone.getTimeZone("UTC"));
		// TODO
		try {
			Date _date = sdf_1.parse(date);
			Date d = new Date(_date.getTime() + second * 1000);
			return sdf_4.format(d);
		} catch (ParseException e) {
			return "";
		}
	}
}
