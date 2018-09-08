package dagger;

import dagger.internal.DaggerCodegen;

public final class Dagger {
  public static <C> C create(Class<C> componentClass) {
    return DaggerCodegen.create(componentClass);
  }

  public static <C, B> B builder(Class<C> componentClass, Class<B> builderClass) {
    return DaggerCodegen.builder(componentClass, builderClass);
  }

  private Dagger() {}
}
