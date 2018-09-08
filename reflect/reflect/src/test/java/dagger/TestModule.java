package dagger;

import java.lang.annotation.Retention;
import javax.inject.Provider;
import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Module
abstract class TestModule {
  @Provides static @Hello String provideHelloString() {
    return "Hello";
  }

  @Provides static @World String provideWorldString() {
    return "world";
  }

  @Provides static @Exclamation String provideExclaimationString() {
    return "!";
  }

  @Provides static String provideString(
      @Hello String hello,
      @World Provider<String> world,
      @Exclamation Lazy<String> exclaimation) {
    return hello + ", " + world.get() + exclaimation.get();
  }

  @Binds abstract CharSequence provideCharSequence(String hello);

  @Retention(RUNTIME)
  @Qualifier @interface Hello {}
  @Retention(RUNTIME)
  @Qualifier @interface World {}
  @Retention(RUNTIME)
  @Qualifier @interface Exclamation {}
}
