jsass-5.10.5-PATCH contains a fork of last officially released jsass library with fix for use on mac with Apple M1 chipset.
Sources:
https://gitlab.com/jsass/jsass/-/issues/94

Compiled fork:
https://github.com/tveimo/jsass


Before that, code could not run and was producing error:

context with path [/xbrl_app] threw exception [Filter execution threw an exception] with root cause
	java.lang.UnsatisfiedLinkError: Can't load library: /Users/markostrisko/Desktop/XbrlConnect/apache-tomcat-9.0.41/temp/libjsass-1712076535857801079/libjsass.dylib
		at java.base/java.lang.ClassLoader.loadLibrary(ClassLoader.java:2633)
		at java.base/java.lang.Runtime.load0(Runtime.java:768)
		at java.base/java.lang.System.load(System.java:1837)
		at io.bit3.jsass.adapter.NativeLoader.loadLibrary(NativeLoader.java:48)
		at io.bit3.jsass.adapter.NativeAdapter.<clinit>(NativeAdapter.java:28)
		at io.bit3.jsass.Compiler.<init>(Compiler.java:35)
		at to.etc.domui.sass.JSassCompiler.compiler(JSassCompiler.java:69)
...


