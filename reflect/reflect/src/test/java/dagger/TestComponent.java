package dagger;

@Component(modules = TestModule.class)
interface TestComponent {
  CharSequence hello();
}

