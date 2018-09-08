package dagger.reflect;

import dagger.Lazy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.inject.Provider;

/** A key and its lookup type. */
final class Request {
  static Request fromMethodParameter(Method method, int parameterIndex) {
    Annotation qualifier = Util.findQualifier(method.getParameterAnnotations()[parameterIndex]);

    Type type = method.getGenericParameterTypes()[parameterIndex];
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

    return new Request(new Key(qualifier, type), lookup);
  }

  private final Key key;
  private final Lookup lookup;

  Request(Key key, Lookup lookup) {
    this.key = key;
    this.lookup = lookup;
  }

  Object resolve(InstanceGraph instanceGraph) {
    return lookup.lookup(instanceGraph, key);
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
