package org.coredata.core.util.event;

public class Event implements IEvent {

	private Type type;

	private String receiver;

	private String title;

	private String content;

	public Event() {

	}

	public Event(Type type, String content) {
		this(type, null, null, content);
	}

	public Event(Type type, String receiver, String title, String content) {
		this.type = type;
		this.content = content;
		this.receiver = receiver;
		this.title = title;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Event [type=" + type + ", receiver=" + receiver + ", content=" + content + "]";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
