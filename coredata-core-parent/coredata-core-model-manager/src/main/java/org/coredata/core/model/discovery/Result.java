package org.coredata.core.model.discovery;

import java.io.Serializable;

public class Result implements Serializable {

	private static final long serialVersionUID = 8691008861937362973L;

	private String errcode;

	private String errmsg;

	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

}
