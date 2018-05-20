package my.domui.app.core.db;

import to.etc.domui.hibernate.config.HibernateConfigurator;

final public class HibernateConfiguration {
    private HibernateConfiguration() {}

    public static final void configure() {
        HibernateConfigurator.addClasses(DbGroup.class);
        HibernateConfigurator.addClasses(DbGroupMember.class);
        HibernateConfigurator.addClasses(DbPermission.class);
        HibernateConfigurator.addClasses(DbUser.class);
    }
}
