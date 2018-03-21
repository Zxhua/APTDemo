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
        //声明构造方法
        MethodSpec.Builder constructorBuidler = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC) // 设置方法修饰符
                .addStatement("routeMap = new $T<>()", HashMap.class);//添加方法体 routeMap 会在后面的代码进行声明
        //遍历Map 并将所有元素导入routeMap中
        for (String key : nameMap.keySet()) {
            String name = nameMap.get(key);
            constructorBuidler.addStatement("routeMap.put(\"$N\", \"$N\")", key, name);
        }
        //获得构造方法
        MethodSpec constructorName = constructorBuidler.build();

        //声明公共方法getActivityName
        MethodSpec routeName = MethodSpec.methodBuilder("getActivityName")
                .addModifiers(PUBLIC)
                .returns(String.class) // 设置返回值类型为String类型
                .addParameter(String.class, "routeName") // 设置 参数类型与参数名称
                .beginControlFlow("if (null != routeMap && !routeMap.isEmpty())") //beginControlFlow 控制流语句 for,while什么的写起来也是这么简单。
                .addStatement("return (String)routeMap.get(routeName)") // 添加方法体
                .endControlFlow()//结束控制流
                .addStatement("return \"\"")
                .build();

        //构建类AnnotationRoute$Finder
        TypeSpec typeSpec = TypeSpec.classBuilder("AnnotationRoute$Finder")
                .addModifiers(PUBLIC)
                .addMethod(constructorName)// 添加构造方法
                .addMethod(routeName)//添加方法
                .addField(HashMap.class, "routeMap", PRIVATE) // 添加变量 roteMap
                .build();

        // 生成文件
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
