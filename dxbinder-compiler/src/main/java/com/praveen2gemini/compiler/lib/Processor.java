package com.praveen2gemini.compiler.lib;

import com.praveen2gemini.lib.annotations.BindView;
import com.praveen2gemini.lib.annotations.Keep;
import com.praveen2gemini.lib.annotations.OnBroadcast;
import com.praveen2gemini.lib.annotations.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * @author Praveen Kumar Sugumaran
 */
public class Processor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;
    private static final Pattern SPECIAL_CHARACTER_REGEX = Pattern.compile("[^a-zA-Z0-9]");

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    /*
     * annotations: list of unique annotations that are getting processed
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {

            // find all the classes that uses the supported annotations
            Set<TypeElement> typeElements = ProcessingUtils.getTypeElementsToProcess(
                    roundEnv.getRootElements(),
                    annotations);

            // for each such class create a wrapper class for binding
            for (TypeElement typeElement : typeElements) {
                String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                String typeName = typeElement.getSimpleName().toString();
                ClassName className = ClassName.get(packageName, typeName);

                ClassName generatedClassName = ClassName
                        .get(packageName, NameStore.getGeneratedClassName(typeName));

                // define the wrapper class
                TypeSpec.Builder classBuilder = TypeSpec.classBuilder(generatedClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Keep.class);

                // add constructor
                classBuilder.addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(className, NameStore.Variable.ANDROID_ACTIVITY)
                        .addStatement("$N($N)",
                                NameStore.Method.BIND_VIEWS,
                                NameStore.Variable.ANDROID_ACTIVITY)
                        .addStatement("$N($N)",
                                NameStore.Method.BIND_ON_CLICKS,
                                NameStore.Variable.ANDROID_ACTIVITY)
                        .addStatement("$N($N)",
                                NameStore.Method.BIND_ON_RECEIVERS,
                                NameStore.Variable.ANDROID_ACTIVITY)
                        .build());

                // add method that maps the views with id
                MethodSpec.Builder bindViewsMethodBuilder = MethodSpec
                        .methodBuilder(NameStore.Method.BIND_VIEWS)
                        .addModifiers(Modifier.PRIVATE)
                        .returns(void.class)
                        .addParameter(className, NameStore.Variable.ANDROID_ACTIVITY);

                for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                    BindView bindView = variableElement.getAnnotation(BindView.class);
                    if (bindView != null) {
                        bindViewsMethodBuilder.addStatement("$N.$N = ($T)$N.findViewById($L)",
                                NameStore.Variable.ANDROID_ACTIVITY,
                                variableElement.getSimpleName(),
                                variableElement,
                                NameStore.Variable.ANDROID_ACTIVITY,
                                bindView.value());
                    }
                }
                classBuilder.addMethod(bindViewsMethodBuilder.build());

                // add method that attaches the onClickListeners
                ClassName androidOnClickListenerClassName = ClassName.get(
                        NameStore.Package.ANDROID_VIEW,
                        NameStore.Class.ANDROID_VIEW,
                        NameStore.Class.ANDROID_VIEW_ON_CLICK_LISTENER);

                ClassName androidViewClassName = ClassName.get(
                        NameStore.Package.ANDROID_VIEW,
                        NameStore.Class.ANDROID_VIEW);

                MethodSpec.Builder bindOnClicksMethodBuilder = MethodSpec
                        .methodBuilder(NameStore.Method.BIND_ON_CLICKS)
                        .addModifiers(Modifier.PRIVATE)
                        .returns(void.class)
                        .addParameter(className, NameStore.Variable.ANDROID_ACTIVITY, Modifier.FINAL);

                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    OnClick onClick = executableElement.getAnnotation(OnClick.class);
                    if (onClick != null) {
                        TypeSpec OnClickListenerClass = TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(androidOnClickListenerClassName)
                                .addMethod(MethodSpec.methodBuilder(NameStore.Method.ANDROID_VIEW_ON_CLICK)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(androidViewClassName, NameStore.Variable.ANDROID_VIEW)
                                        .addStatement("$N.$N($N)",
                                                NameStore.Variable.ANDROID_ACTIVITY,
                                                executableElement.getSimpleName(),
                                                NameStore.Variable.ANDROID_VIEW)
                                        .returns(void.class)
                                        .build())
                                .build();
                        bindOnClicksMethodBuilder.addStatement("$N.findViewById($L).setOnClickListener($L)",
                                NameStore.Variable.ANDROID_ACTIVITY,
                                onClick.value(),
                                OnClickListenerClass);
                    }
                }
                classBuilder.addMethod(bindOnClicksMethodBuilder.build());


                ClassName androidBroadcastReceiverClassName = ClassName.get(
                        NameStore.Package.ANDROID_CONTENT,
                        NameStore.Class.ANDROID_BROADCAST_RECEIVER);

                ClassName androidIntentFilterClassName = ClassName.get(
                        NameStore.Package.ANDROID_CONTENT,
                        NameStore.Class.ANDROID_INTENT_FILTER);

                ClassName androidContextClassName = ClassName.get(
                        NameStore.Package.ANDROID_CONTENT,
                        NameStore.Class.ANDROID_CONTEXT);

                ClassName androidIntentClassName = ClassName.get(
                        NameStore.Package.ANDROID_CONTENT,
                        NameStore.Class.ANDROID_INTENT);

                MethodSpec.Builder bindOnReceiversMethodBuilder = MethodSpec
                        .methodBuilder(NameStore.Method.BIND_ON_RECEIVERS)
                        .addModifiers(Modifier.PRIVATE)
                        .returns(void.class)
                        .addParameter(className, NameStore.Variable.ANDROID_ACTIVITY, Modifier.FINAL);

                MethodSpec.Builder unbindAllMethodBuilder = MethodSpec
                        .methodBuilder(NameStore.Method.UNBIND_ALL)
                        .addModifiers(Modifier.PRIVATE)
                        .returns(void.class)
                        .addParameter(className, NameStore.Variable.ANDROID_ACTIVITY, Modifier.FINAL);

                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    OnBroadcast onBroadcast = executableElement.getAnnotation(OnBroadcast.class);
                    if (onBroadcast != null) {

                        String broadcasterVariableName = onBroadcast.value().replaceAll(SPECIAL_CHARACTER_REGEX.pattern(), "_").toLowerCase(Locale.ENGLISH);
                        FieldSpec fieldSpec = FieldSpec.builder(ClassName.get(NameStore.Package.ANDROID_CONTENT, NameStore.Class.ANDROID_BROADCAST_RECEIVER),
                                broadcasterVariableName, Modifier.PRIVATE).build();
                        classBuilder.addField(fieldSpec);

                        TypeSpec onBroadcastClass = TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(androidBroadcastReceiverClassName)
                                .addMethod(MethodSpec.methodBuilder(NameStore.Method.ANDROID_VIEW_ON_RECEIVER)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(androidContextClassName, NameStore.Variable.ANDROID_CONTEXT)
                                        .addParameter(androidIntentClassName, NameStore.Variable.ANDROID_INTENT)
                                        .addStatement("$N.$N($N)",
                                                NameStore.Variable.ANDROID_ACTIVITY,
                                                executableElement.getSimpleName(),
                                                NameStore.Variable.ANDROID_INTENT)
                                        .returns(void.class)
                                        .build())
                                .build();
                        TypeSpec onIntentFilterClass = TypeSpec.anonymousClassBuilder(String.format("\"%s\"", onBroadcast.value()))
                                .addSuperinterface(androidIntentFilterClassName)
                                .build();
                        bindOnReceiversMethodBuilder.addStatement(broadcasterVariableName + " = $L", onBroadcastClass);

                        bindOnReceiversMethodBuilder.addStatement("$N.registerReceiver($N, $L)",
                                NameStore.Variable.ANDROID_ACTIVITY,
                                fieldSpec,
                                onIntentFilterClass);

                        unbindAllMethodBuilder.addStatement("$N.unregisterReceiver($N)", NameStore.Variable.ANDROID_ACTIVITY, fieldSpec);
                    }
                }
                classBuilder.addMethod(bindOnReceiversMethodBuilder.build());
                classBuilder.addMethod(unbindAllMethodBuilder.build());

                // write the defines class to a java file
                try {
                    JavaFile.builder(packageName,
                            classBuilder.build())
                            .build()
                            .writeTo(filer);
                } catch (IOException e) {
                    messager.printMessage(Diagnostic.Kind.ERROR, e.toString(), typeElement);
                }
            }
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new TreeSet<>(Arrays.asList(
                BindView.class.getCanonicalName(),
                OnClick.class.getCanonicalName(),
                OnBroadcast.class.getCanonicalName(),
                Keep.class.getCanonicalName()));
    }
}
