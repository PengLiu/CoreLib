package org.coredata.core.data.readers.crawler;

public class SpiderLogin {
	private String url;
	private String username;
	private String password;
	private String username_css;
	private String password_css;
	private String login_btn_css;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername_css() {
		return username_css;
	}

	public void setUsername_css(String username_css) {
		this.username_css = username_css;
	}

	public String getPassword_css() {
		return password_css;
	}

	public void setPassword_css(String password_css) {
		this.password_css = password_css;
	}

	public String getLogin_btn_css() {
		return login_btn_css;
	}

	public void setLogin_btn_css(String login_btn_css) {
		this.login_btn_css = login_btn_css;
	}

}
