package org.stevens.cs562.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class DynamicCompiler {

	String path  ;
	String compile_path;
	String main_class_name;
	
	public DynamicCompiler(String main_class_name) throws IOException {
		this.path = StringBuilder.getFinalPath(ResourceHelper.getValue("output"));
		this.compile_path = this.path + "output/class";
		this.main_class_name = main_class_name;
	}
	
	public void compileAndRun() {
			System.out.println("[INFO] ############################################################################### ");
			System.out.println("[INFO] #######################             Show Result             ################### ");
			System.out.println("[INFO] ############################################################################### ");
			System.out.println();
			try {
				compile();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(IllegalStateException e) {
				e.printStackTrace();
			}
	}
	
	private void compile() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		File current_compile_path = new File(compile_path);
		File postgresql_lib = new File(System.getProperty("user.dir") + "\\lib\\postgresql-9.4-1201.jdbc41.jar");
		if(current_compile_path.exists()) {
			current_compile_path.deleteOnExit();
			current_compile_path.mkdirs();
		}
		
		DiagnosticListener<JavaFileObject> diaglistener = new MyDiagnosticListener();
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diaglistener, null, null);
		List<String> optionList = new ArrayList<String>();
		optionList.add("-d");
		optionList.add(current_compile_path.getAbsolutePath());
		optionList.add("-cp");
        optionList.add(System.getProperty("java.class.path") + ";dist/InlineCompiler.jar;" + System.getProperty("user.dir") + "\\lib\\postgresql-9.4-1201.jdbc41.jar");
//       System.out.println(optionList);
//       System.out.println(System.getProperty("user.dir"));
        Iterable<? extends JavaFileObject> compilationUnit  = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(loadAllJavaFiles()));
        JavaCompiler.CompilationTask task = compiler.getTask(
        		null, 
                fileManager, 
                diaglistener, 
                optionList, 
                null, 
                compilationUnit);
        if (task.call()) {
			URLClassLoader classLoader = new URLClassLoader(new URL[]{current_compile_path.toURI().toURL(), postgresql_lib.toURI().toURL()});
        	 Class<?> cls = classLoader.loadClass(this.main_class_name);
        	 Method main = cls.getDeclaredMethod("main", String[].class); // get the main method using reflection
        	 Object[] args = new Object[] { new String[0] };
        	 main.invoke(null, args);
        	 classLoader.close();
        	 
        }
	}
	
	private class MyDiagnosticListener implements DiagnosticListener<JavaFileObject> {

		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			System.out.println(diagnostic.toString());
			
		}
		
	}
	
	private File[] loadAllJavaFiles() {
		File folder = new File(path);
		File[] files = folder.listFiles(new JavaFileFilter(this.main_class_name.toLowerCase().startsWith("emf")));
		return files;
	}
	
	private class JavaFileFilter implements FilenameFilter {
		
		private String prefix;
		
		public JavaFileFilter(boolean emf) {
			if(emf) {
				this.prefix = "emf";
			} else {
				this.prefix = "mf";
			}
			
		}

		public boolean accept(File dir, String name) {
			return name.toLowerCase().startsWith(this.prefix) && name.toLowerCase().endsWith("java");
		}
		
	}

}
