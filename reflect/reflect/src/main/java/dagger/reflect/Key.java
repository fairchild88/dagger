package dagger.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.jetbrains.annotations.Nullable;

/** A type and optional qualifier. */
final class Key {
  static Key fromMethodParameter(Method method, int parameterIndex) {
    Annotation qualifier = Util.findQualifier(method.getParameterAnnotations()[parameterIndex]);
    Type type = method.getGenericParameterTypes()[parameterIndex];
    return new Key(qualifier, type);
  }

  private final @Nullable Annotation qualifer;
  private final Type type;

  Key(@Nullable Annotation qualifer, Type type) {
    this.qualifer = qualifer;
    this.type = type;
  }

  @Override public String toString() {
    if (qualifer != null) {
      return "@" + qualifer + " " + type;
    }
    return type.toString();
  }

  @Override public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof Key)) return false;
    Key other = (Key) obj;
    return type.equals(other.type)
        && Util.equals(qualifer, other.qualifer);
  }

  @Override public int hashCode() {
    return 31 * type.hashCode() + (qualifer != null ? qualifer.hashCode() : 0);
  }
}
