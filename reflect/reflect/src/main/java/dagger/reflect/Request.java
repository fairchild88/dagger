package dagger.reflect;

import com.google.auto.value.AutoValue;
import dagger.Lazy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.inject.Provider;
import org.jetbrains.annotations.NotNull;

/** A key and its lookup type. */
@AutoValue
abstract class Request {
  static Request fromMethodParameter(Method method, int parameterIndex) {
    return fromTypeAndAnnotations(
        method.getGenericParameterTypes()[parameterIndex],
        method.getParameterAnnotations()[parameterIndex]);
  }

  static Request fromConstructorParameter(Constructor<?> constructor, int parameterIndex) {
    return fromTypeAndAnnotations(
        constructor.getGenericParameterTypes()[parameterIndex],
        constructor.getParameterAnnotations()[parameterIndex]);
  }

  private static Request fromTypeAndAnnotations(Type type, Annotation[] annotations) {
    Lookup lookup = Lookup.INSTANCE;
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      if (rawType == Provider.class) {
        type = parameterizedType.getActualTypeArguments()[0];
        lookup = Lookup.PROVIDER;
      } else if (rawType == Lazy.class) {
        type = parameterizedType.getActualTypeArguments()[0];
        lookup = Lookup.LAZY;
      }
    }

    Annotation qualifier = Util.findQualifier(annotations);
    return new AutoValue_Request(Key.of(qualifier, type), lookup);
  }

  static Request of(Key key, Lookup lookup) {
    return new AutoValue_Request(key, lookup);
  }

  abstract Key key();
  abstract Lookup lookup();

  Object resolve(InstanceGraph instanceGraph) {
    return lookup().lookup(instanceGraph, key());
  }

  enum Lookup {
    INSTANCE {
      @Override Object lookup(InstanceGraph instanceGraph, Key key) {
        return instanceGraph.getInstance(key);
      }
    },
    PROVIDER {
      @Override Object lookup(InstanceGraph instanceGraph, Key key) {
        return instanceGraph.getProvider(key);
      }
    },
    LAZY {
      @Override Object lookup(InstanceGraph instanceGraph, Key key) {
        return instanceGraph.getLazy(key);
      }
    };

    abstract Object lookup(InstanceGraph instanceGraph, Key key);
  }
}
