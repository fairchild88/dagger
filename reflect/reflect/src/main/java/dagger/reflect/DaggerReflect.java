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
import javax.inject.Provider;

import static dagger.reflect.Util.findQualifier;
import static dagger.reflect.Util.findScope;
import static dagger.reflect.Util.hasAnnotation;
import static dagger.reflect.Util.tryInvoke;
import static java.lang.reflect.Modifier.ABSTRACT;

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
      Collections.addAll(moduleQueue, module.includes());
      // TODO subcomponents

      for (final Method method : moduleClass.getDeclaredMethods()) {
        Type returnType = method.getGenericReturnType();
        Annotation[] annotations = method.getDeclaredAnnotations();
        Annotation qualifier = findQualifier(annotations);
        int parameterCount = method.getParameterTypes().length;

        if (hasAnnotation(annotations, Provides.class)) {
          final Request[] requests = new Request[parameterCount];
          for (int i = 0; i < parameterCount; i++) {
            requests[i] = Request.fromMethodParameter(method, i);
          }

          // TODO check visibility
          method.setAccessible(true);

          Annotation methodScope = findScope(annotations);
          if (!Util.equals(scope, methodScope)) {
            // TODO real error message
            throw new IllegalStateException("Cannot provide " + methodScope + " in " + scope);
          }

          instanceGraph.put(new Key(qualifier, returnType), new Provider<Object>() {
            @Override public Object get() {
              Object[] arguments = new Object[requests.length];
              for (int i = 0; i < requests.length; i++) {
                arguments[i] = requests[i].resolve(instanceGraph);
              }
              return tryInvoke(method, null, arguments);
            }
          });
          continue;
        }

        if (hasAnnotation(annotations, Binds.class)) {
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

          Annotation methodScope = findScope(annotations);
          if (!Util.equals(scope, methodScope)) {
            // TODO real error message
            throw new IllegalStateException("Cannot provide " + methodScope + " in " + scope);
          }

          final Key delegate = Key.fromMethodParameter(method, 0);
          instanceGraph.put(new Key(qualifier, returnType), new Provider<Object>() {
            @Override public Object get() {
              return instanceGraph.getInstance(delegate);
            }
          });
          continue;
        }

        throw notImplemented("Method " + method);
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
