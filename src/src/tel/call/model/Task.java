package tel.call.model;

import java.util.Date;

/**
 * 
 * @author huangxin (3203317@qq.com)
 * 
 */
public class Task {
	private String id;
	private String task_name;
	private String task_content;
	private Date issued_time;
	private Integer status;
	private Date create_time;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTask_name() {
		return task_name;
	}

	public void setTask_name(String task_name) {
		this.task_name = task_name;
	}

	public String getTask_content() {
		return task_content;
	}

	public void setTask_content(String task_content) {
		this.task_content = task_content;
	}

	public Date getIssued_time() {
		return issued_time;
	}

	public void setIssued_time(Date issued_time) {
		this.issued_time = issued_time;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
}
