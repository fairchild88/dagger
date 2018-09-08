package dagger.reflect;

import dagger.Lazy;
import dagger.internal.DoubleCheck;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

final class InstanceGraph {
  private final @Nullable InstanceGraph parent;
  private final ConcurrentHashMap<Key, Provider<?>> bindings =
      new ConcurrentHashMap<Key, Provider<?>>();

  InstanceGraph() {
    this(null);
  }

  InstanceGraph(@Nullable InstanceGraph parent) {
    this.parent = parent;
  }

  void put(Key key, Provider<?> provider) {
    if (bindings.put(key, provider) != null) {
      throw new IllegalStateException("Duplicate binding for " + key);
    }
  }

  Object getInstance(Key key) {
    return getProvider(key).get();
  }

  Provider<?> getProvider(Key key) {
    Provider<?> provider = bindings.get(key);
    if (provider == null && parent != null) {
      provider = parent.getProvider(key);
    }
    if (provider == null) {
      throw new IllegalStateException("No binding for " + key);
    }
    return provider;
  }

  Lazy<?> getLazy(Key key) {
    return DoubleCheck.lazy(getProvider(key));
  }
}
