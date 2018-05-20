package to.etc.domui.component.upload;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.ConversationContext;

public interface IUploadAcceptingComponent {
	boolean handleUploadRequest(@NonNull RequestContextImpl param, @NonNull ConversationContext conversation) throws Exception;

}
