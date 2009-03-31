package to.etc.domui.server.parts;

public class PartFactoryNotFoundException extends Exception
{
	PartFactoryNotFoundException(String name)
	{
		super("Parts factory '"+name+"' not found.");
	}
}
