package to.etc.domui.hibernate.config;

import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.Map;

import javax.persistence.*;
import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.hibernate.cfg.*;
import org.hibernate.mapping.*;
import org.hibernate.property.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.hibernate.types.*;
import to.etc.util.*;

/**
 * This class attempts to check/correct common hibernate errors that it itself is too stupid to check for.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 5, 2012
 */
final public class HibernateChecker {
	private Configuration m_config;
	private int m_dupTables;
	private int m_badOneToMany;
	private int m_badChildType;

	private boolean m_reportProblems;

	private int m_badJoinColumn;

	private int m_enumErrors;

	private int m_dateErrors;

	private int m_missingEntity;

	private int m_badBooleans;

	private int m_missingColumn;

	private Class< ? > m_currentClass;

	private PropertyInfo m_currentProperty;

	private int m_domuiMetaFatals;

	private static enum Severity {
		INFO, WARNING, ERROR, MUSTFIXNOW
	}

	public HibernateChecker(Configuration config, boolean reportProblems) {
		m_config = config;
		m_reportProblems = reportProblems;
	}

	private void problem(Severity sev, String s) {
		if(m_reportProblems) {
			StringBuilder sb = new StringBuilder();
			sb.append(sev.name());
			sb.append(": ").append(s);
			if(m_currentClass != null) {
				sb.append(" class ").append(m_currentClass.getName());
				if(m_currentProperty != null) {
					sb.append(" property ").append(m_currentProperty.getName());
				}
			}

			System.out.println("MAPPING " + sb.toString());
		}
	}

	public void enhanceMappings() throws Exception {
		m_config.buildMappings();

		/*
		 * This code completes configuration for some user types, because Hibernate is unable to pass
		 * sufficient information to user types (like the property type, sigh). This code prevents us
		 * from having to set the user type by hand which is one big error time sink.
		 */
		Map<String, Class< ? >> exmaps = new HashMap<String, Class< ? >>();
		m_dupTables = 0;
		m_badOneToMany = 0;
		m_badChildType = 0;

		//-- For some reason the hibernate kids only fill config after the session factory has been created. Very unhygienic.
		for(Iterator< ? > iter = m_config.getClassMappings(); iter.hasNext();) {
			PersistentClass pc = (PersistentClass) iter.next();
			m_currentClass = pc.getMappedClass();
			m_currentProperty = null;

			String tn = pc.getTable().getName();
			Class< ? > xcl = exmaps.get(tn.toLowerCase());
			if(xcl != null) {
				m_dupTables++;
				problem(Severity.ERROR, "DUPLICATE TABLE IN HIBERNATE MAPPING: " + tn + " in " + xcl + " and " + pc.getMappedClass());
			}
			exmaps.put(tn.toLowerCase(), pc.getMappedClass());

			//-- Handle nasty types.
			Entity ent = (Entity) pc.getMappedClass().getAnnotation(Entity.class);
			if(null == ent) {
				problem(Severity.ERROR, "Class " + pc.getClassName() + " added without @Entity annotation - is this a real table class??");
				m_missingEntity++;
			}

			//-- Check property annotations.
			List<PropertyInfo> pilist = ClassUtil.calculateProperties(pc.getMappedClass());
			for(PropertyInfo pi : pilist) {
				m_currentProperty = pi;
				Method g = pi.getGetter();
				if(g != null) {
					checkOneToMany(g);
					checkEnumMapping(g);
					checkDateMapping(g);
					checkBooleanMapping(g);
				}
			}

			checkDomuiMetadata();

			for(Iterator< ? > iter2 = pc.getPropertyIterator(); iter2.hasNext();) {
				Property property = (Property) iter2.next();
				//				System.out.println("... " + property.getName() + " type " + property.getType().getName());
				Getter g = property.getGetter(pc.getMappedClass());

				Method method = g.getMethod();
				Class< ? > actual = null == method ? null : method.getReturnType();

				if(property.getType().getName().equals(MappedEnumType.class.getName()) || "nl.itris.viewpoint.db.hibernate.ViewPointMappedEnumType".equals(property.getType().getName())) {
					//-- Sigh.. Try to obtain the property's actual type from the getter because Hibernate does not have an easy route to it, appearently.
					SimpleValue v = (SimpleValue) property.getValue();
					//					System.out.println("Property " + v + " is " + v.getTypeName() + " class=" + actual);
					if(v.getTypeParameters() == null)
						v.setTypeParameters(new Properties());
					v.getTypeParameters().setProperty("propertyType", actual.getName());
				}
				if(actual != null && List.class.isAssignableFrom(actual)) {
					Bag many = (Bag) property.getValue();
					many.setTypeName("to.etc.domui.hibernate.types.ObservableListType");
				}


			}
		}
		if(m_reportProblems)
			report();
	}

	/**
	 * boolean columns cannot be mapped to Boolean but must be mapped to boolean with a proper @Type.
	 * @param g
	 */
	private void checkBooleanMapping(Method g) {
		if(Boolean.class.isAssignableFrom(g.getReturnType())) {
			Transient tr = g.getAnnotation(Transient.class);
			if(null == tr) {
				problem(Severity.WARNING, "Do not use Boolean wrappers. Use boolean as God intended.");
				m_badBooleans++;
			} else {
				m_badBooleans++;
				problem(Severity.ERROR, "boolean column mapped to Boolean wrapper is invalid- map to boolean using BooleanPrimitiveYNType");

				//-- getter must be named "is"
				if(!g.getName().startsWith("is")) {
					problem(Severity.WARNING, "boolean property's getter must be called isXxxx(), not getXxxx()");
				}
			}
		}
		if(boolean.class.isAssignableFrom(g.getReturnType())) {
			//-- Must have proper mapping/type.
			Transient tr = g.getAnnotation(Transient.class);
			if(tr == null) {
				Column col = g.getAnnotation(Column.class);
				if(null == col) {
					m_missingColumn++;
					problem(Severity.ERROR, "Missing @Column annotation");
				} else {
					boolean nullable = col.nullable();
					if(nullable) {
						org.hibernate.annotations.Type ty = g.getAnnotation(org.hibernate.annotations.Type.class);
						if(ty == null) {
							m_badBooleans++;
							problem(Severity.ERROR, "Missing @Type on nullable primitive boolean!");
						} else {
							String ttn = ty.type();
							if(ttn.equals("yes_no")) {
								problem(Severity.ERROR, "@Type(yes_no) on nullable primitive boolean!");
								m_badBooleans++;
							}
						}
					}
				}
			}

			//-- getter must be named "is"
			if(!g.getName().startsWith("is")) {
				problem(Severity.WARNING, "boolean property's getter must be called isXxxx(), not getXxxx()");
			}
		}
	}

	/**
	 * OneToMany: must have mappedBy, cannot have JoinColumn, must have List<T> resultType.
	 * @param g
	 */
	private void checkOneToMany(Method g) {
		javax.persistence.OneToMany o2m = g.getAnnotation(javax.persistence.OneToMany.class);
		if(o2m != null) {
			if(o2m.mappedBy().length() == 0) {
				m_badOneToMany++;
				problem(Severity.ERROR, "Missing 'mappedBy' in @OneToMany annotation- REPLACE WITH mappedBy and parent property in child class");
			}

			//-- Only type allowed is List<T>
			if(!List.class.isAssignableFrom(g.getReturnType())) {
				problem(Severity.ERROR, "Only java.collections.List<T> allowed as @OneToMany type");
				m_badChildType++;
			}

			//-- JoinColumn not allowed
			javax.persistence.JoinColumn jc = g.getAnnotation(javax.persistence.JoinColumn.class);
			if(null != jc) {
				problem(Severity.ERROR, "@JoinColumn found on @OneToMany - not allowed. Use mappedBy");
				m_badJoinColumn++;
			}
		}
	}

	/**
	 * Date columns must be that, must have a proper @DateType
	 * @param g
	 */
	private void checkDateMapping(Method g) {
		if(Date.class.isAssignableFrom(g.getReturnType())) {
			if(g.getReturnType() != Date.class) {
				m_dateErrors++;
				problem(Severity.ERROR, "Type for a date property MUST be java.util.Date");
			}

			Temporal tt = g.getAnnotation(Temporal.class);
			if(null == tt) {
				m_dateErrors++;
				problem(Severity.WARNING, "Missing @Temporal annotation on Date column");
			} else if(tt.value() == TemporalType.TIME) {
				m_dateErrors++;
				problem(Severity.WARNING, "@TemporalType of type TIME is stupid on date field.");
			}
		}
	}

	/**
	 * Enums must be mapped as String, not ORDINAL.
	 * @param g
	 */
	private void checkEnumMapping(Method g) {
		if(Enum.class.isAssignableFrom(g.getReturnType())) {		// Is type enum?
			//-- If the enum has a @Type we will have to assume the type handles mapping correctly (like MappedEnumType)
			org.hibernate.annotations.Type ht = g.getAnnotation(Type.class);
			if(null == ht) {
				//-- No @Type mapping, so this must have proper @Enumerated definition.
				Enumerated e = g.getAnnotation(Enumerated.class);
				if(null == e) {
					problem(Severity.ERROR, "Missing @Enumerated annotation on enum property - this will cause ORDINAL mapping of an enum which is undesirable");
					m_enumErrors++;
				} else if(e.value() != EnumType.STRING) {
					problem(Severity.ERROR, "@Enumerated(ORDINAL) annotation on enum property - this will cause ORDINAL mapping of an enum which is undesirable");
					m_enumErrors++;
				}
			}
		}
	}

	public void checkDomuiMetadata() {
		try {
			m_currentProperty = null;
			ClassMetaModel cmm = MetaManager.findClassMeta(m_currentClass);
		} catch(Exception x) {
			problem(Severity.MUSTFIXNOW, "DomUI Metamodel error: " + x.toString());
			x.printStackTrace();
			m_domuiMetaFatals++;
		}
	}

	public void report() {
		if(getBadOneToMany() > 0)
			System.out.println("MAPPING: " + getBadOneToMany() + " bad @OneToMany mappings with missing mappedBy");
		if(getBadChildType() > 0)
			System.out.println("MAPPING: " + getBadChildType() + " bad @OneToMany mappings with non-List<T> type");
		if(getBadJoinColumn() > 0)
			System.out.println("MAPPING: " + getBadJoinColumn() + " bad @OneToMany mappings with @JoinColumn");
		System.out.println("MAPPING: " + getDupTables() + " duplicate tables");
		System.out.println("MAPPING: " + getEnumErrors() + " enum's mapped as ORDINAL or missing @Enumerated annotation");
		System.out.println("MAPPING: " + getDateErrors() + " date field without proper @Temporal annotation or of the wrong date type");
		if(getMissingEntity() > 0)
			System.out.println("MAPPING: " + getMissingEntity() + " classes missing an @Entity annotation");
		if(getBadBooleans() > 0)
			System.out.println("MAPPING: " + getBadBooleans() + " bad Boolean/boolean mappings");
		if(getMissingColumn() > 0)
			System.out.println("MAPPING: " + getMissingColumn() + " properties with a missing @Column annotation");
		if(getDomuiMetaFatals() > 0)
			System.out.println("MAPPING: " + getDomuiMetaFatals() + " fatal DomUI metamodel errors - must be fixed now.");
	}

	public int getDomuiMetaFatals() {
		return m_domuiMetaFatals;
	}

	public int getMissingColumn() {
		return m_missingColumn;
	}

	public int getBadBooleans() {
		return m_badBooleans;
	}

	public int getMissingEntity() {
		return m_missingEntity;
	}

	public int getDateErrors() {
		return m_dateErrors;
	}

	public int getEnumErrors() {
		return m_enumErrors;
	}

	public int getBadOneToMany() {
		return m_badOneToMany;
	}

	public int getBadChildType() {
		return m_badChildType;
	}

	public int getDupTables() {
		return m_dupTables;
	}

	public int getBadJoinColumn() {
		return m_badJoinColumn;
	}
}

