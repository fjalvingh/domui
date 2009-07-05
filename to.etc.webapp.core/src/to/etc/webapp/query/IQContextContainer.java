package to.etc.webapp.query;

public interface IQContextContainer {
	QDataContext internalGetSharedContext();

	void internalSetSharedContext(QDataContext c);

	QDataContextSource internalGetContextSource();

	void internalSetContextSource(QDataContextSource s);
}
