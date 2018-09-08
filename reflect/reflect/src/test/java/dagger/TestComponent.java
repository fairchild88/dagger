package dagger;

import java.lang.annotation.Retention;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Component(modules = TestModule.class)
interface TestComponent {
  CharSequence hello();
}

@Module
abstract class TestModule {
  @Provides static @Greeting String provideGreetingString(EnglishGreeting greeting) {
    return greeting.get();
  }

  @Provides static @Target String provideTargetString() {
    return "world";
  }

  @Provides static @Exclamation String provideExclaimationString() {
    return "!";
  }

  @Provides static String provideString(
      @Greeting String greeting,
      @Target Provider<String> target,
      @Exclamation Lazy<String> exclaimation) {
    return greeting + ", " + target.get() + exclaimation.get();
  }

  @Binds abstract CharSequence provideCharSequence(String hello);

  @Retention(RUNTIME)
  @Qualifier @interface Greeting {}
  @Retention(RUNTIME)
  @Qualifier @interface Target {}
  @Retention(RUNTIME)
  @Qualifier @interface Exclamation {}
}

class EnglishGreeting {
  @Inject EnglishGreeting() {}

  String get() {
    return "Hello";
  }
}
