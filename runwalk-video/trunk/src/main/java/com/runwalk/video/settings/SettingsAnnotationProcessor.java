package com.runwalk.video.settings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.common.collect.Sets;

@SupportedAnnotationTypes({"javax.xml.bind.annotation.XmlRootElement"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class SettingsAnnotationProcessor extends AbstractProcessor {

	private final Set<String> packageNames = Sets.newHashSet();
	
	private FileObject outputFile;
	
	private OutputStream outputStream;
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		final String packageName = this.getClass().getPackage().getName();
		try {
			Filer filer = processingEnv.getFiler();
			outputFile = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, "jaxbPackageNames.txt");
			outputStream = outputFile.openOutputStream();
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error creating resource file " + packageName);
		}
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return super.getSupportedAnnotationTypes();
	}

	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		boolean result = false;
		Filer filer = processingEnv.getFiler();
		Messager messager = processingEnv.getMessager();
		try(OutputStream outputStream = this.outputStream) {
			for (TypeElement te : annotations) {
				for (Element element : roundEnv.getElementsAnnotatedWith(te)) {
					Element enclosingElement = element.getEnclosingElement();
					ElementKind enclosingElementKind = enclosingElement.getKind();
					if (enclosingElementKind.isClass()) {
						enclosingElement = enclosingElement.getEnclosingElement();
					}
					if (enclosingElementKind == ElementKind.PACKAGE) {
						String packageName = enclosingElement.asType().toString();
						updateJaxbIndexFile(packageName, element);
						updateJaxbPackagesFile(packageName, element);
					}
				}
			}
		} catch (IOException e1) {
			messager.printMessage(Diagnostic.Kind.ERROR, "Error occured: " + e1.getMessage());
		} 
		return result;
	}
	
	private boolean updateJaxbPackagesFile(String packageName, Element element) throws IOException {
		Messager messager = processingEnv.getMessager();
		boolean result = true;
		if (!packageNames.contains(packageName)) {
			messager.printMessage(Diagnostic.Kind.NOTE, "Found package: " + packageName);
			packageNames.add(packageName);
			String string = packageName + System.getProperty("line.separator");
			outputStream.write(string.getBytes());
		}		
		return result;
	}

	private void updateJaxbIndexFile(String packageName, Element e) throws IOException { 
		Filer filer = processingEnv.getFiler();
		Messager messager = processingEnv.getMessager();
		FileObject resource = null;
		try {
			resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageName, "jaxb.index");
		} catch (IOException e1) {
			// file could not be openend.. create one
			resource = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, "jaxb.index");
		}
		// filer should be kept open...
	}

}
