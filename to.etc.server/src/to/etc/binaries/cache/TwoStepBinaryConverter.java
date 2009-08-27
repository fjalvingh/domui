package to.etc.binaries.cache;

public interface TwoStepBinaryConverter extends BinaryConverter {
	public ConverterResult calculate(BinaryInfo bicore, String type, String mime, int w, int h) throws Exception;
}
