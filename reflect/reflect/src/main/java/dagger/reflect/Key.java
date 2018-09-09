package dagger.reflect;

import com.google.auto.value.AutoValue;
import dagger.reflect.TypeUtil.ParameterizedTypeImpl;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/** A type and optional qualifier. */
@AutoValue
abstract class Key {
  static Key fromMethodParameter(Method method, int parameterIndex) {
    Annotation qualifier = Util.findQualifier(method.getParameterAnnotations()[parameterIndex]);
    Type type = method.getGenericParameterTypes()[parameterIndex];
    return new AutoValue_Key(qualifier, type);
  }

  static Key of(@Nullable Annotation qualifier, Type type) {
    return new AutoValue_Key(qualifier, type);
  }

  static Key setOf(Key key) {
    Type setType = new ParameterizedTypeImpl(null, Set.class, key.type());
    return new AutoValue_Key(key.qualifer(), setType);
  }

  abstract @Nullable Annotation qualifer();
  abstract Type type();
}
