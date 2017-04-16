package to.etc.domui.component.upload;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.state.*;

public interface IUploadAcceptingComponent {
	boolean handleUploadRequest(@Nonnull RequestContextImpl param, @Nonnull ConversationContext conversation) throws Exception;

}
