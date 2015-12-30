package tel.call.util;

import java.util.Date;

import android.app.Application;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class UserInfo extends Application {

	private String apikey;
	private String seckey;
	private long ts;

	private CallInfo callInfo;

	public CallInfo getCallInfo() {
		return callInfo;
	}

	public void setCallInfo(CallInfo callInfo) {
		this.callInfo = callInfo;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts - (new Date()).getTime();
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getSeckey() {
		return seckey;
	}

	public void setSeckey(String seckey) {
		this.seckey = seckey;
	}

	public class CallInfo {

		private String handtask_id;
		private String tel_num;
		private int talk_time_len;

		public int getTalk_time_len() {
			return talk_time_len;
		}

		public void setTalk_time_len(int talk_time_len) {
			this.talk_time_len = talk_time_len;
		}

		public String getHandtask_id() {
			return handtask_id;
		}

		public void setHandtask_id(String handtask_id) {
			this.handtask_id = handtask_id;
		}

		public String getTel_num() {
			return tel_num;
		}

		public void setTel_num(String tel_num) {
			this.tel_num = tel_num;
		}
	}
}