package stream.io;

public class WavFileException extends Exception {

	/** The unique class ID */
	private static final long serialVersionUID = 7075277548183485665L;

	public WavFileException() {
		super();
	}

	public WavFileException(String message) {
		super(message);
	}

	public WavFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public WavFileException(Throwable cause) {
		super(cause);
	}
}
