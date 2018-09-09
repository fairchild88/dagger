package dagger.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import javax.inject.Inject;

import static dagger.reflect.Util.tryNewInstance;
import static dagger.reflect.Util.validateVisibility;

final class JustInTimeBinding extends Binding<Object> {
  private final Key key;
  private Constructor<?> constructor;

  JustInTimeBinding(Key key) {
    this.key = key;
  }

  @Override protected Request[] initialize(Annotation scope) {
    Type targetType = key.type();
    if (key.qualifer() != null || !(targetType instanceof Class<?>)) {
      throw new IllegalStateException("No binding for " + key);
    }
    Class<?> targetClass = (Class<?>) targetType;

    // TODO validate scope

    Constructor<?> constructor = null;
    for (Constructor<?> candidate : targetClass.getDeclaredConstructors()) {
      if (candidate.getAnnotation(Inject.class) != null) {
        if (constructor != null) {
          throw new IllegalStateException(targetClass + " defines multiple @Inject constructors");
        }
        constructor = candidate;
      }
    }
    if (constructor == null) {
      throw new IllegalStateException(targetClass + " has no @Inject constructor");
    }
    validateVisibility(constructor);
    this.constructor = constructor;

    int parameterCount = constructor.getParameterTypes().length;
    Request[] requests = new Request[parameterCount];
    for (int i = 0; i < parameterCount; i++) {
      requests[i] = Request.fromConstructorParameter(constructor, i);
    }
    return requests;
  }

  @Override protected Object resolve(Object[] dependencies) {
    return tryNewInstance(constructor, dependencies);
  }

  @Override public String toString() {
    return "JustInTimeBinding[" + key + "]";
  }
}
