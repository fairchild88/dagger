package dagger.reflect;

import java.lang.reflect.Constructor;
import javax.inject.Inject;

import static dagger.reflect.Util.tryNewInstance;

final class JustInTimeBinding extends Binding<Object> {
  private final Key key;
  private Constructor<?> constructor;

  JustInTimeBinding(Key key) {
    this.key = key;
  }

  @Override protected Request[] initialize() {
    if (key.qualifer() == null && key.type() instanceof Class<?>) {
      Class<?> target = (Class<?>) key.type();

      Constructor<?> constructor = null;
      for (Constructor<?> candidate : target.getDeclaredConstructors()) {
        if (candidate.getAnnotation(Inject.class) != null) {
          if (constructor != null) {
            throw new IllegalStateException(target + " defines multiple @Inject constructors");
          }
          constructor = candidate;
        }
      }
      if (constructor == null) {
        throw new IllegalStateException(target + " has no @Inject constructor");
      }

      // TODO check visibility
      constructor.setAccessible(true);

      this.constructor = constructor;

      int parameterCount = constructor.getParameterTypes().length;
      Request[] requests = new Request[parameterCount];
      for (int i = 0; i < parameterCount; i++) {
        requests[i] = Request.fromConstructorParameter(constructor, i);
      }
      return requests;
    }

    throw new IllegalStateException("No binding for " + key);
  }

  @Override protected Object resolve(Object[] dependencies) {
    return tryNewInstance(constructor, dependencies);
  }
}
