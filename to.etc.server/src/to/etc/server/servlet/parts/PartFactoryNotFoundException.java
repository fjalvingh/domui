package to.etc.server.servlet.parts;

public class PartFactoryNotFoundException extends Exception {
	PartFactoryNotFoundException(String name) {
		super("Parts factory '" + name + "' not found.");
	}
}
