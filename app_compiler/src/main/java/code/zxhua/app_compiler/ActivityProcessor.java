package code.zxhua.app_compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import code.zxhua.app_annation.ActivityMap;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

@SupportedAnnotationTypes("code.zxhua.app_annation.ActivityMap")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ActivityProcessor extends AbstractProcessor {

    private static final String PAGECONFIG = "PageConfig.java";

    private HashMap<String, String> pageMap = new HashMap<>();


    private Filer mFiler;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        System.out.println("ActivityProcessor .....");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        PrintStream ps = null;

        try {
            ps = new PrintStream(new FileOutputStream(PAGECONFIG));

            for (Element t : roundEnv.getElementsAnnotatedWith(ActivityMap.class)) {
                ExecutableElement executableElement = (ExecutableElement) t;
                Name clazzName = executableElement.getSimpleName();

                ActivityMap activityMap = t.getAnnotation(ActivityMap.class);
                String pageName = activityMap.value();

                pageMap.put(clazzName.toString(), pageName);
            }

            generateJavaFile(pageMap);

        } catch (Exception e) {
            System.out.println(e.getClass() + ":" + e.getMessage());
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }


    private void generateJavaFile(Map<String, String> nameMap) {
        //�������췽��
        MethodSpec.Builder constructorBuidler = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC) // ���÷������η�
                .addStatement("routeMap = new $T<>()", HashMap.class);//��ӷ����� routeMap ���ں���Ĵ����������
        //����Map ��������Ԫ�ص���routeMap��
        for (String key : nameMap.keySet()) {
            String name = nameMap.get(key);
            constructorBuidler.addStatement("routeMap.put(\"$N\", \"$N\")", key, name);
        }
        //��ù��췽��
        MethodSpec constructorName = constructorBuidler.build();

        //������������getActivityName
        MethodSpec routeName = MethodSpec.methodBuilder("getActivityName")
                .addModifiers(PUBLIC)
                .returns(String.class) // ���÷���ֵ����ΪString����
                .addParameter(String.class, "routeName") // ���� �����������������
                .beginControlFlow("if (null != routeMap && !routeMap.isEmpty())") //beginControlFlow ��������� for,whileʲô��д����Ҳ����ô�򵥡�
                .addStatement("return (String)routeMap.get(routeName)") // ��ӷ�����
                .endControlFlow()//����������
                .addStatement("return \"\"")
                .build();

        //������AnnotationRoute$Finder
        TypeSpec typeSpec = TypeSpec.classBuilder("AnnotationRoute$Finder")
                .addModifiers(PUBLIC)
                .addMethod(constructorName)// ��ӹ��췽��
                .addMethod(routeName)//��ӷ���
                .addField(HashMap.class, "routeMap", PRIVATE) // ��ӱ��� roteMap
                .build();

        // �����ļ�
        JavaFile javaFile = JavaFile.builder("code.lib.apt.route", typeSpec)
                                    .build();
        try {
            javaFile.writeTo(mFiler);
            System.out.println("AnnotationRoute$Finder builder complete");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("AnnotationRoute$Finder builder failure");
        }
    }

}
