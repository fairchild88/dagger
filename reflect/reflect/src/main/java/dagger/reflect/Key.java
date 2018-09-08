package dagger.reflect;

import com.google.auto.value.AutoValue;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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

  abstract @Nullable Annotation qualifer();
  abstract Type type();
}
