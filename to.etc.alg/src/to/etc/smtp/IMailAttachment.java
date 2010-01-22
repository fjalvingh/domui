package to.etc.smtp;

import java.io.*;

public interface IMailAttachment {
	InputStream getInputStream() throws Exception;

	String getMime();

	String getIdent();
}
