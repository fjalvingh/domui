package to.etc.iocular.web;

import javax.servlet.ServletContext;

import to.etc.iocular.def.BasicContainerBuilder;
import to.etc.iocular.def.ContainerDefinition;

public class BasicWebConfigurator implements WebConfigurator {
	final public WebConfiguration createConfiguration(ServletContext ctx) throws Exception {
		BasicContainerBuilder	b = BasicContainerBuilder.createBuilder("applicationContainer");
		defineApplication(b);
		ContainerDefinition		app = b.createDefinition();

		b = BasicContainerBuilder.createChildBuilder(app, "sessionContainer");
		defineSession(b);
		ContainerDefinition	ses	= b.createDefinition();

		b = BasicContainerBuilder.createChildBuilder(ses, "requestContainer");
		defineRequest(b);
		ContainerDefinition	req	= b.createDefinition();

		return new WebConfiguration(app, ses, req);
	}

	public void	defineApplication(BasicContainerBuilder b) {
	}

	public void	defineSession(BasicContainerBuilder b) {
	}
	
	public void	defineRequest(BasicContainerBuilder b) {
	}
}
