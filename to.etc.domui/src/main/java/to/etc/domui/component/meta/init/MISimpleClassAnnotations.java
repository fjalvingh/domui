package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.impl.DefaultClassMetaModel;
import to.etc.domui.trouble.Trouble;
import to.etc.domui.util.DomUtil;
import to.etc.util.StringTool;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-10-17.
 */
public class MISimpleClassAnnotations extends AbstractClassAnnotationProvider {
	@Override protected void decodeClassAnnotation(MetaInitContext context, DefaultClassMetaModel cmm, Annotation an, String name) throws Exception {
		if("javax.persistence.Table".equals(name)) {
			//-- Decode fields from the annotation.
			decodeTableAnnotation(cmm, an);
		} else if("to.etc.webapp.qsql.QJdbcTable".equals(name)) {
			cmm.setPersistentClass(true);
		}
	}

	private void decodeTableAnnotation(@Nonnull final DefaultClassMetaModel cmm, @Nonnull final Annotation an) {
		//-- Decode fields from the annotation.
		if(cmm.getTableName() != null)
			return;

		try {
			String tablename = (String) DomUtil.getClassValue(an, "name");
			String tableschema = (String) DomUtil.getClassValue(an, "schema");
			if(tablename != null) {
				if(!StringTool.isBlank(tableschema))
					tablename = tableschema + "." + tablename;
				cmm.setTableName(tablename);
			}
		} catch(Exception x) {
			Trouble.wrapException(x);
		}
	}
}
