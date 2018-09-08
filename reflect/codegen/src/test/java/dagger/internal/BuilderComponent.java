package dagger.internal;

import dagger.Component;

@Component
interface BuilderComponent {
  @Component.Builder
  interface Builder {
    BuilderComponent build();
  }
}
