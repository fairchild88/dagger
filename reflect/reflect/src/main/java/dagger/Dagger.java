package dagger;

import dagger.reflect.DaggerReflect;

public final class Dagger {
  public static <C> C create(Class<C> componentClass) {
    return DaggerReflect.create(componentClass);
  }

  public static <C, B> B builder(Class<C> componentClass, Class<B> builderClass) {
    return DaggerReflect.builder(componentClass, builderClass);
  }

  private Dagger() {}
}
