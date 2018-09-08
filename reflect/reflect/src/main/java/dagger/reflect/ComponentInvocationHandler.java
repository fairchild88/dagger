package dagger.reflect;

import dagger.Subcomponent;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static dagger.reflect.DaggerReflect.notImplemented;
import static dagger.reflect.Util.findQualifier;

final class ComponentInvocationHandler implements InvocationHandler {
  private final InstanceGraph instanceGraph;

  ComponentInvocationHandler(InstanceGraph instanceGraph) {
    this.instanceGraph = instanceGraph;
  }

  @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }
    if (method.isDefault()) {
      throw notImplemented("Default methods");
    }

    Type returnType = method.getGenericReturnType();
    Class<?>[] parameterTypes = method.getParameterTypes();

    if (args != null
        && args.length == 1
        && (returnType == void.class || returnType.equals(parameterTypes[0]))) {
      throw notImplemented("Members injection");
    }

    if (args == null || args.length == 0) {
      Annotation[] annotations = method.getDeclaredAnnotations();
      if (returnType instanceof Class<?>
          && ((Class<?>) returnType).getAnnotation(Subcomponent.class) != null) {
        throw notImplemented("Subcomponents");
      }

      Key key = Key.of(findQualifier(annotations), returnType);
      return instanceGraph.getInstance(key);
    }

    throw notImplemented("Method " + method);
  }
}
