package dagger.reflect.compiler;

import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public final class DaggerReflectCompilerTest {
  @Test public void simple() {
    JavaFileObject component = JavaFileObjects.forSourceString("example.TestComponent", ""
        + "package example;\n"
        + "\n"
        + "import dagger.Component;\n"
        + "\n"
        + "@Component\n"
        + "interface TestComponent {\n"
        + "}\n"
    );

    JavaFileObject expected = JavaFileObjects.forSourceString("example.DaggerTestComponent", ""
        + "package example;\n"
        + "\n"
        + "import dagger.reflect.DaggerReflect\n"
        + "\n"
        + "final class DaggerTestComponent {\n"
        + "  public static TestComponent create() {\n"
        + "    return DaggerReflect.create(TestComponent.class);\n"
        + "  }\n"
        + "}\n"
    );

    assertAbout(javaSource())
        .that(component)
        .processedWith(new DaggerReflectCompiler())
        .compilesWithoutError()
        .and()
        .generatesSources(expected);
  }

  @Test public void builder() {
    JavaFileObject component = JavaFileObjects.forSourceString("example.TestComponent", ""
        + "package example;\n"
        + "\n"
        + "import dagger.Component;\n"
        + "\n"
        + "@Component\n"
        + "interface TestComponent {\n"
        + "  @Component.Builder\n"
        + "  interface Builder {\n"
        + "  }\n"
        + "}\n"
    );

    JavaFileObject expected = JavaFileObjects.forSourceString("example.DaggerTestComponent", ""
        + "package example;\n"
        + "\n"
        + "import dagger.reflect.DaggerReflect\n"
        + "\n"
        + "final class DaggerTestComponent {\n"
        + "  public static TestComponent create() {\n"
        + "    return DaggerReflect.create(TestComponent.class);\n"
        + "  }\n"
        + "\n"
        + "  public static TestComponent.Builder builder() {\n"
        + "    return DaggerReflect.builder(TestComponent.class, TestComponent.Builder.class);\n"
        + "  }\n"
        + "}\n"
    );

    assertAbout(javaSource())
        .that(component)
        .processedWith(new DaggerReflectCompiler())
        .compilesWithoutError()
        .and()
        .generatesSources(expected);
  }
}
