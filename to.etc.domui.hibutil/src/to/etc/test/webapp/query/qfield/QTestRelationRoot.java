package to.etc.test.webapp.query.qfield;
import to.etc.webapp.query.*;
import javax.annotation.*;
/**
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 */
@Generated(value = { "This is a generated file. It will be overwritten during compilation. It is therefore useless to make any modifications" })
public final class QTestRelationRoot extends QTestRelation<QTestRelationRoot> {

	QTestRelationRoot() {
		super(null, null, null);
	}

	public @Nonnull
	QCriteria<to.etc.test.webapp.query.qfield.TestRelation> getCriteria() {
		validateGetCriteria();
		return (QCriteria<to.etc.test.webapp.query.qfield.TestRelation>) m_criteria;
	}
}