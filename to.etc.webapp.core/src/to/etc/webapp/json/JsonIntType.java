package to.etc.webapp.json;

import javax.annotation.*;

import to.etc.lexer.*;

public class JsonIntType implements ITypeMapping {

	@Override
	public void render(@Nonnull JsonWriter w, @Nonnull Object instance) throws Exception {
		Number n = (Number) instance;
		w.writeNumber(n);
	}

	@Override
	public Object parse(@Nonnull JsonReader reader) throws Exception {
		if(reader.getLastToken() != ReaderScannerBase.T_NUMBER)
			throw new JsonParseException(reader, this, "Expecting an integer");
		Integer res = Integer.decode(reader.getCopied());
		reader.nextToken();
		return res;
	}
}
