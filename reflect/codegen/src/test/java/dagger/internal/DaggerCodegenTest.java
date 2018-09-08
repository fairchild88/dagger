package dagger.internal;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class DaggerCodegenTest {
  @Test public void create() {
    NoBuilderComponent actual = DaggerCodegen.create(NoBuilderComponent.class);
    assertThat(actual).isInstanceOf(DaggerNoBuilderComponent.class);
  }

  @Test public void builder() {
    BuilderComponent.Builder actual =
        DaggerCodegen.builder(BuilderComponent.class, BuilderComponent.Builder.class);
    assertThat(actual).isInstanceOf(DaggerBuilderComponent.builder().getClass());
  }
}
