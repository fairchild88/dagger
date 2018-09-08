package dagger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public final class DaggerTest {
  @Parameters(name = "{0}")
  public static Object[] parameters() {
    return Frontend.values();
  }

  @Parameter public Frontend frontend;

  @Test public void hello() {
    TestComponent component = frontend.create(TestComponent.class);
    assertThat(component.hello()).isEqualTo("Hello, world!");
  }
}
