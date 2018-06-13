package org.coredata.core.model.discovery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Resultarea implements Serializable {

	private static final long serialVersionUID = 1847525530309946807L;

	private String id;

	private String title;

	private String asserttype;

	private String cmd;

	private String withheader;

	private String expected;

	private List<Result> result = new ArrayList<>();

	private List<Param> param;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExpected() {
		return expected;
	}

	public void setExpected(String expected) {
		this.expected = expected;
	}

	public List<Result> getResult() {
		return result;
	}

	public void setResult(List<Result> result) {
		this.result = result;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getAsserttype() {
		return asserttype;
	}

	public void setAsserttype(String asserttype) {
		this.asserttype = asserttype;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getWithheader() {
		return withheader;
	}

	public void setWithheader(String withheader) {
		this.withheader = withheader;
	}

	public List<Param> getParam() {
		return param;
	}

	public void setParam(List<Param> param) {
		this.param = param;
	}

}
