package dagger.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.Util.validateVisibility;
import static java.lang.reflect.Modifier.ABSTRACT;

final class BindsBinding extends Binding<Object> {
  private final Method method;

  BindsBinding(Method method) {
    this.method = method;
  }

  @Override protected Request[] initialize(@Nullable Annotation scope) {
    int parameterCount = method.getParameterTypes().length;
    if (parameterCount != 1) {
      throw new IllegalStateException("@Binds must have single parameter: "
          + method.getDeclaringClass().getName()
          + '.'
          + method.getName());
    }
    if ((method.getModifiers() & ABSTRACT) == 0) {
      throw new IllegalStateException("@Binds methods must be abstract: "
          + method.getDeclaringClass()
          + '.'
          + method.getName());
    }

    validateVisibility(method);

    // TODO validate scope

    Key delegate = Key.fromMethodParameter(method, 0);
    return new Request[] { Request.of(delegate, Request.Lookup.INSTANCE) };
  }

  @Override protected Object resolve(Object[] dependencies) {
    return dependencies[0];
  }

  @Override public String toString() {
    return "BindsBinding[" + method + "]";
  }
}
