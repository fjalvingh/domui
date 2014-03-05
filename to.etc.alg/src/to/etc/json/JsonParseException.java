package to.etc.json;

public class JsonParseException extends RuntimeException {

	public JsonParseException() {
		super();
	}

	public JsonParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonParseException(String message) {
		super(message);
	}

	public JsonParseException(Throwable cause) {
		super(cause);
	}

	public JsonParseException(JsonReader reader, ITypeMapping jsonClassType, String string) {
		super(string);
	}
}
