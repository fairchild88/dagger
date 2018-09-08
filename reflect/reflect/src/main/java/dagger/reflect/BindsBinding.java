package dagger.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static dagger.reflect.Util.findScope;
import static java.lang.reflect.Modifier.ABSTRACT;

final class BindsBinding extends Binding<Object> {
  private final Method method;
  private final Annotation scope;

  BindsBinding(Method method, Annotation scope) {
    this.method = method;
    this.scope = scope;
  }

  @Override protected Request[] initialize() {
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

    // TODO check visibility
    method.setAccessible(true);

    Annotation[] annotations = method.getDeclaredAnnotations();
    Annotation methodScope = findScope(annotations);
    if (!Util.equals(scope, methodScope)) {
      // TODO real error message
      throw new IllegalStateException("Cannot provide " + methodScope + " in " + scope);
    }

    Key delegate = Key.fromMethodParameter(method, 0);
    return new Request[] { Request.of(delegate, Request.Lookup.INSTANCE) };
  }

  @Override protected Object resolve(Object[] dependencies) {
    return dependencies[0];
  }
}
