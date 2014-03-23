package nl.itris.qd.astparse;

import java.io.*;
import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.*;
import org.eclipse.text.edits.*;

import to.etc.util.*;

public class AstParse {
	static private final boolean DEBUG = false;

	static private final Map<String, String> PRIM2WRAP = new HashMap<String, String>();
	static {
		PRIM2WRAP.put("byte", "Byte");
		PRIM2WRAP.put("char", "Character");
		PRIM2WRAP.put("short", "Short");
		PRIM2WRAP.put("int", "Integer");
		PRIM2WRAP.put("long", "Long");
		PRIM2WRAP.put("boolean", "Boolean");
		PRIM2WRAP.put("float", "Float");
		PRIM2WRAP.put("double", "Double");
//		PRIM2WRAP.put("", "");
//		PRIM2WRAP.put("", "");
//		PRIM2WRAP.put("", "");
	}

	private AST m_ast;

	private boolean m_haschanges;

	public static void main(String[] args) throws Exception {
		new AstParse().run(args);
	}

	private void run(String[] args) throws Exception {
		if(args.length == 0) {
			System.out.println("Usage: AstParse [list-of-files]");
			System.exit(10);
		}

		for(String srcf : args) {
			File src = new File(srcf);
			if(!src.exists())
				throw new RuntimeException(src + ": file does not exist.");
			handleItem(src);
		}
//
//		File src = new File("../../vp/moca.database/src/nl/itris/viewpoint/db/wfl/WorkflowActivityDefinition.java").getCanonicalFile();
//
////		File src = new File(args[0]);
//		updateFile(src);
	}

	private void handleItem(File src) throws Exception {
		if(src.isFile()) {
			if(src.getName().endsWith(".java"))
				updateFile(src);
			return;
		}

		for(File f : src.listFiles()) {
			handleItem(f);
		}
	}


	private void updateFile(File src) throws Exception {
		m_haschanges = false;
		System.out.println("Updating: " + src);
		String text = FileTool.readFileAsString(src, "utf-8");
		Document doc = new Document(text);

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(doc.get().toCharArray());
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.recordModifications();
		m_ast = cu.getAST();
//		ASTRewrite rew = ASTRewrite.create(m_ast);

		List<AbstractTypeDeclaration> types = cu.types();
		for(AbstractTypeDeclaration type : types) {
			if(type.getNodeType() == ASTNode.TYPE_DECLARATION) {
				System.out.println("Class def: " + type.getName());

				//-- We need to implement IObservableEntity
				TypeDeclaration td = (org.eclipse.jdt.core.dom.TypeDeclaration) type;
				List<SimpleType> superInterfaces = td.superInterfaceTypes();

				boolean isObservable = false;

				for(Type tt : superInterfaces) {
					if(tt instanceof SimpleType) {
						SimpleType t = (SimpleType) tt;
						System.out.println(" >> " + t.getName() + ", " + t.getClass());
						//					ITypeBinding binding = t.resolveBinding();
						//					String qname = binding.getQualifiedName();
						if(t.getName().getFullyQualifiedName().equals("IObservableEntity")) {
							isObservable = true;
							break;
						}
					}
				}

				if(!isObservable) {
					// Add IObservable interface
					SimpleType st = m_ast.newSimpleType(m_ast.newSimpleName("IObservableEntity"));
					superInterfaces.add(st);

					//-- Make sure imports contain proper thingy.
					addImport(cu, "to.etc.domui.databinding.observables");
				}

				// Class def found
				List<BodyDeclaration> bodies = type.bodyDeclarations();
				for(BodyDeclaration body : bodies) {
					if(body.getNodeType() == ASTNode.METHOD_DECLARATION) {
						MethodDeclaration method = (MethodDeclaration) body;
						String methodName = method.getName().getFullyQualifiedName();
						if(methodName.startsWith("set")) {
//							System.out.println("name: " + methodName);
							checkMethod(method);
						}
					}
				}
			}
		}

		TextEdit edits = cu.rewrite(doc, null);
		edits.apply(doc);
		String newsource = doc.get();
		if(!m_haschanges || newsource.equals(text))
			return;

		System.out.println("Changed " + src);
		if(DEBUG) {
			System.out.println(newsource);
			System.exit(10);
		}

		FileTool.writeFileFromString(src, newsource, "utf-8");
	}

	private void addImport(CompilationUnit cu, String impname) {
		List<ImportDeclaration> imports = cu.imports();
		for(ImportDeclaration id: imports) {
			String name = id.getName().getFullyQualifiedName();
			System.out.println("import>> " + name + " : " + id + ", " + id.getName().getClass());
			if(name.equals(impname))
				return;
		}

		ImportDeclaration id = m_ast.newImportDeclaration();
		id.setName(createQualifiedName(impname));
		id.setOnDemand(true);
		imports.add(id);
	}

	private Name createQualifiedName(String path) {
		String[] ar = path.split("\\.");
		if(ar.length == 0)
			throw new IllegalStateException("Bad qualified name: " + path);
		Name curr = null;
		for(int i = 0; i < ar.length; i++) {
			SimpleName sn = m_ast.newSimpleName(ar[i]);					// Current level's name
			if(curr == null) {
				curr = sn;
			} else {
				curr = m_ast.newQualifiedName(curr, sn);
			}
		}
		return curr;
	}


	private void checkMethod(MethodDeclaration method) {
		//-- Must be "void" type.
		List<Modifier> modifiers = method.modifiers();
		if(modifiers.size() == 0)
			return;
		boolean ispublic = false;
		for(Modifier mo : modifiers) {
			if(mo.getKeyword().toString().equals("public"))
				ispublic = true;
		}
		if(!ispublic)
			return;

		//-- Check return type.
		Type rv = method.getReturnType2();
		if(!rv.isPrimitiveType())
			return;
		PrimitiveType pt = (PrimitiveType) rv;
		if(pt.getPrimitiveTypeCode() != PrimitiveType.VOID)
			return;

//		System.out.println("   modifiers=" + modifiers + ", type=" + rv);

		//-- Arguments
		List<ASTNode> an = (List<ASTNode>) method.getStructuralProperty(MethodDeclaration.PARAMETERS_PROPERTY);
		if(an.size() != 1)
			return;

		//-- get argument type
		SingleVariableDeclaration arg = (SingleVariableDeclaration) an.get(0);
		Type type = arg.getType();

//		System.out.println("Arg is " + arg + " of type " + type);
		if(type.toString().contains("List"))
			return;

		//-- On to the statement list...
		Block body = method.getBody();
		List<ASTNode> stmt = (List<ASTNode>) body.getStructuralProperty(Block.STATEMENTS_PROPERTY);
		if(stmt.size() != 1)						// We only expect 'ExpressionStatement: field = parameter'
			return;
		ASTNode exan = stmt.get(0);
		if(!(exan instanceof ExpressionStatement))
			return;

		ExpressionStatement ex = (ExpressionStatement) exan;
		Expression expression = ex.getExpression();
		if(!(expression instanceof Assignment))
			return;

		//-- Ok; we're there. Start modifying.
		String pre;
		if(type.toString().toLowerCase().contains("boolean"))
			pre = "is";
		else
			pre = "get";

		String methodName = method.getName().getFullyQualifiedName();
		String nwname = pre + methodName.substring(3);


		//-- Create: [type] oldv = getXxx();
		VariableDeclarationStatement oldvd;

		//-- Determine type: use wrapper where needed
		String wrap = null;
		if(type.isPrimitiveType()) {
			PrimitiveType primt = (PrimitiveType) type;
			wrap = PRIM2WRAP.get(type.toString());
			if(null == wrap)
				throw new IllegalStateException("Cannot locate wrapper type for primitive " + type);
			SimpleType nwtype = m_ast.newSimpleType(m_ast.newSimpleName(wrap));

			MethodInvocation valueof = m_ast.newMethodInvocation();
			valueof.setExpression(m_ast.newSimpleName(wrap));				// Integer.
			valueof.setName(m_ast.newSimpleName("valueOf"));				// valueOf

			MethodInvocation callget = m_ast.newMethodInvocation();
			callget.setName(m_ast.newSimpleName(nwname));

			valueof.arguments().add(callget);

			VariableDeclarationFragment frag = m_ast.newVariableDeclarationFragment();
			frag.setName(m_ast.newSimpleName("oldv"));
			frag.setInitializer(valueof);

			oldvd = m_ast.newVariableDeclarationStatement(frag);
			oldvd.setType(nwtype);
		} else {
			VariableDeclarationFragment frag = m_ast.newVariableDeclarationFragment();
			frag.setName(m_ast.newSimpleName("oldv"));

			MethodInvocation callget = m_ast.newMethodInvocation();
			callget.setName(m_ast.newSimpleName(nwname));

			frag.setInitializer(callget);

			oldvd = m_ast.newVariableDeclarationStatement(frag);
			SimpleType nwtype = m_ast.newSimpleType(m_ast.newSimpleName(type.toString()));
			oldvd.setType(nwtype);
		}
		m_haschanges = true;

		body.statements().add(0, oldvd);

		//-- Now add: firePropertyChange("autoFinish", oldv, v);
		MethodInvocation fireme = m_ast.newMethodInvocation();
		fireme.setName(m_ast.newSimpleName("firePropertyChange"));

		StringLiteral sl1 = m_ast.newStringLiteral();
		String propname = methodName.substring(3);
		propname = propname.substring(0, 1).toLowerCase() + propname.substring(1);
		sl1.setLiteralValue(propname);
		fireme.arguments().add(sl1);

		fireme.arguments().add(m_ast.newSimpleName("oldv"));
		if(type.isPrimitiveType()) {
			//-- generate Wrapper.valueOf(parametername)
			if(null == wrap)
				throw new IllegalStateException("Wrapper not known");

			MethodInvocation valueof = m_ast.newMethodInvocation();
			valueof.setExpression(m_ast.newSimpleName(wrap));				// Integer.
			valueof.setName(m_ast.newSimpleName("valueOf"));				// valueOf
			valueof.arguments().add(m_ast.newSimpleName(arg.getName().getFullyQualifiedName()));		// parameter variable

			fireme.arguments().add(valueof);

		} else {
			//-- Just generate the parameter variable name
			fireme.arguments().add(m_ast.newSimpleName(arg.getName().getFullyQualifiedName()));
		}

		ExpressionStatement firest = m_ast.newExpressionStatement(fireme);

		body.statements().add(firest);

//		System.out.println("Body " + body);


	}

}
