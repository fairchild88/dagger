package dagger.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static dagger.reflect.Util.findScope;
import static dagger.reflect.Util.tryInvoke;

final class ProvidesBinding extends Binding<Object> {
  private final Method method;
  private final Annotation scope;

  ProvidesBinding(Method method, Annotation scope) {
    this.method = method;
    this.scope = scope;
  }

  @Override protected Request[] initialize() {
    // TODO check visibility
    method.setAccessible(true);

    Annotation[] annotations = method.getDeclaredAnnotations();
    Annotation methodScope = findScope(annotations);
    if (!Util.equals(scope, methodScope)) {
      // TODO real error message
      throw new IllegalStateException("Cannot provide " + methodScope + " in " + scope);
    }

    int parameterCount = method.getParameterTypes().length;
    Request[] requests = new Request[parameterCount];
    for (int i = 0; i < parameterCount; i++) {
      requests[i] = Request.fromMethodParameter(method, i);
    }
    return requests;
  }

  @Override protected Object resolve(Object[] dependencies) {
    return tryInvoke(method, null, dependencies);
  }
}
