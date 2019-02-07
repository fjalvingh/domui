package to.etc.fixdom;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileWriter;
import java.util.EnumSet;
import java.util.Optional;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 7-2-19.
 */
public class FixDom {
	static public void main(String[] args) throws Exception {
		new FixDom().run();


	}

	/**
	 *
	 */
	private void run() throws Exception {
		File root = new File("/home/jal/git/domui/re/to.etc.domui/src/main/java/to/etc/domui/dom");


		File file = new File(root, "css/CssBase.java");
		fixBaseFile(file);
	}

	private void fixBaseFile(File file) throws Exception {
		if(! file.exists())
			throw new IllegalStateException(file + ": does not exist");

		CompilationUnit parse = JavaParser.parse(file);

		Optional<PackageDeclaration> pd = parse.getPackageDeclaration();
		if(! pd.isPresent()) {
			error(file, "No package declaration");
			return;
		}
		PackageDeclaration packageDeclaration = pd.get();
		String id = packageDeclaration.getName().asString();

		//-- Find the root class
		for(TypeDeclaration<?> type : parse.getTypes()) {
			System.out.println("type: " + type.getNameAsString());

			//-- We need to rename the type to AbstractCssBase and make it abstract
			type.setName(new SimpleName("Abstract") + type.getNameAsString());

			//-- It needs to become generic <N extends itself>
			EnumSet<Modifier> modifiers = type.getModifiers();
			modifiers.add(Modifier.ABSTRACT);
			type.setModifiers(modifiers);

			ClassOrInterfaceDeclaration mdi = (ClassOrInterfaceDeclaration) type;
			NodeList<TypeParameter> typeParameters = mdi.getTypeParameters();

			NodeList<ClassOrInterfaceType> nl = new NodeList<>();
			ClassOrInterfaceType baseType = new ClassOrInterfaceType(type.getNameAsString());
			baseType.setTypeArguments(NodeList.nodeList(new TypeParameter("N")));
			nl.add(baseType);
			TypeParameter tp = new TypeParameter("N", nl);
			typeParameters.add(tp);
			mdi.setTypeParameters(typeParameters);

			//-- Now: fix all methods
			for(MethodDeclaration method : type.getMethods()) {
				fixMethod(method);
			}

			System.out.println(parse.toString());

			File f = new File(file.getParentFile(), type.getNameAsString() + ".java");
			try(FileWriter fw = new FileWriter(f)) {
				fw.write(parse.toString());
			}
		}
	}

	private void fixMethod(MethodDeclaration method) {
		Type type = method.getType();
		if(! type.asString().equals("void"))
			return;

		if(! method.getName().asString().startsWith("set")) {
			return;
		}
		TypeParameter typeN = new TypeParameter("N");
		method.setType(typeN);

		//-- All places where we have a return we need to rewrite


		NodeList<Statement> statements = method.getBody().get().getStatements();
		if(statements.size() != 0) {
			Statement last = statements.get(statements.size() - 1);
			if(last.toString().equals("return")) {
				statements.remove(statements.size()-1);
			}
		}

		//-- Fix any intermediary returns
		CastExpr cx = new CastExpr(typeN, new ThisExpr());
		method.getBody().get().accept(new VoidVisitorAdapter<Void>() {
			@Override public void visit(ReturnStmt n, Void arg) {
				n.setExpression(cx);
				super.visit(n, arg);
			}
		}, null);

		//-- Add the appropriate statement
		ReturnStmt rs = new ReturnStmt(cx);
		statements.add(rs);
	}

	private void error(File file, String err) {
		System.err.println(file + " error: " + err);
	}


}
