package to.etc.webapp.query;

public interface IQContextContainer {
	QDataContext internalGetSharedContext();

	void internalSetSharedContext(QDataContext c);

	QDataContextFactory internalGetDataContextFactory();

	void internalSetDataContextFactory(QDataContextFactory s);
}
