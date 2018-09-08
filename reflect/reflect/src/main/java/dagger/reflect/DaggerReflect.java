package dagger.reflect;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

import static dagger.reflect.Util.findQualifier;
import static dagger.reflect.Util.findScope;
import static dagger.reflect.Util.hasAnnotation;

public final class DaggerReflect {
  public static <C> C create(Class<C> componentClass) {
    if (!componentClass.isInterface()) {
      throw new IllegalArgumentException("Only interface components are supported");
    }

    Component component = componentClass.getAnnotation(Component.class);
    if (component == null) {
      throw new IllegalArgumentException(componentClass + " lacks @Component annotation");
    }

    Class<?>[] dependencies = component.dependencies();
    if (dependencies.length > 0) {
      throw notImplemented("Component dependencies");
    }

    Annotation scope = findScope(componentClass.getAnnotations());

    final InstanceGraph instanceGraph = new InstanceGraph();
    Deque<Class<?>> moduleQueue = new ArrayDeque<Class<?>>();
    Set<Class<?>> seenModules = new LinkedHashSet<Class<?>>();
    Collections.addAll(moduleQueue, component.modules());
    while (!moduleQueue.isEmpty()) {
      Class<?> moduleClass = moduleQueue.removeFirst();
      if (!seenModules.add(moduleClass)) {
        continue;
      }
      if (moduleClass.getSuperclass() != Object.class) {
        throw notImplemented("Module inheritance");
      }
      Module module = moduleClass.getAnnotation(Module.class);
      if (module == null) {
        throw new IllegalStateException("Module " + moduleClass + " missing @Module");
      }
      if (module.subcomponents().length != 0) {
        throw notImplemented("Module subcomponents");
      }
      Collections.addAll(moduleQueue, module.includes());

      for (final Method method : moduleClass.getDeclaredMethods()) {
        Type returnType = method.getGenericReturnType();
        Annotation[] annotations = method.getDeclaredAnnotations();
        Annotation qualifier = findQualifier(annotations);
        Key key = Key.of(qualifier, returnType);

        if (hasAnnotation(annotations, Provides.class)) {
          instanceGraph.put(key, new ProvidesBinding(method, scope));
        } else if (hasAnnotation(annotations, Binds.class)) {
          instanceGraph.put(key, new BindsBinding(method, scope));
        } else {
          throw notImplemented("Method " + method);
        }
      }
    }

    //noinspection unchecked Single interface proxy.
    return (C) Proxy.newProxyInstance(componentClass.getClassLoader(),
        new Class<?>[] { componentClass }, new ComponentInvocationHandler(instanceGraph));
  }

  public static <C, B> B builder(Class<C> componentClass, Class<B> builderClass) {
    throw notImplemented("Builders");
  }

  static RuntimeException notImplemented(String feature) {
    return new UnsupportedOperationException(feature + " does not work yet, sorry!");
  }

  private DaggerReflect() {}
}
