package to.etc.domui.jpa.em;

import to.etc.webapp.query.ICriteriaTableDef;
import to.etc.webapp.query.IQueryExecutor;
import to.etc.webapp.query.IQueryExecutorFactory;
import to.etc.webapp.query.QDataContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-3-18.
 */
public class JpaQueryExecutor {
    public static IQueryExecutorFactory FACTORY = new IQueryExecutorFactory() {
        @Nullable @Override public IQueryExecutor<?> findContextHandler(@Nonnull QDataContext root,
            @Nonnull Class<?> clz) {
            return null;
        }

        @Nullable @Override public IQueryExecutor<?> findContextHandler(@Nonnull QDataContext root,
            @Nonnull Object recordInstance) {
            return null;
        }

        @Nullable @Override public IQueryExecutor<?> findContextHandler(@Nonnull QDataContext root,
            @Nonnull ICriteriaTableDef<?> tableMeta) {
            return null;
        }
    };

}
