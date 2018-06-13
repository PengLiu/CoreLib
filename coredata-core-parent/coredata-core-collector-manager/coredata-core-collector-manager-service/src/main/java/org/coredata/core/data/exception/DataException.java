package org.coredata.core.data.exception;

public class DataException extends RuntimeException {

	private static final long serialVersionUID = 2510267358921118998L;

	private String message;

	public DataException() {
		super();
	}

	public DataException(final String message) {
		super(message);
	}

	public DataException(final Exception e) {
		super(e);
	}

	public DataException(Throwable cause) {
		super(cause);
	}

	public DataException(final String message, final Throwable cause) {
		super(message, cause);
	}

	@Override
	public String getMessage() {
		return this.message == null ? super.getMessage() : this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return this.message;
	}

}
