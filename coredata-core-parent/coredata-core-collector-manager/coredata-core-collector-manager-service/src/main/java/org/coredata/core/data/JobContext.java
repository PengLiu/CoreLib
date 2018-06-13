package org.coredata.core.data;

public class JobContext {
	
	private boolean isReaderFinished;
	
	private boolean isReaderError;
	
	private boolean isWriterFinished;
	
	private boolean isWriterError;

	public boolean isReaderFinished() {
		return isReaderFinished;
	}

	public void setReaderFinished(boolean isReaderFinished) {
		this.isReaderFinished = isReaderFinished;
	}

	public boolean isReaderError() {
		return isReaderError;
	}

	public void setReaderError(boolean isReaderError) {
		this.isReaderError = isReaderError;
	}

	public boolean isWriterFinished() {
		return isWriterFinished;
	}

	public void setWriterFinished(boolean isWriterFinished) {
		this.isWriterFinished = isWriterFinished;
	}

	public boolean isWriterError() {
		return isWriterError;
	}

	public void setWriterError(boolean isWriterError) {
		this.isWriterError = isWriterError;
	}
	
}
