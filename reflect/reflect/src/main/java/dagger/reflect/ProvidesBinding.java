package dagger.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.Util.tryInvoke;
import static dagger.reflect.Util.validateVisibility;

final class ProvidesBinding extends Binding<Object> {
  private final Method method;

  ProvidesBinding(Method method) {
    this.method = method;
  }

  @Override protected Request[] initialize(@Nullable Annotation scope) {
    validateVisibility(method);

    // TODO validate scope

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

  @Override public String toString() {
    return "ProvidesBinding[" + method + "]";
  }
}
