package dagger.reflect;

import dagger.Lazy;
import dagger.internal.DoubleCheck;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

final class InstanceGraph {
  private final @Nullable InstanceGraph parent;
  private final ConcurrentHashMap<Key, Binding<?>> bindings =
      new ConcurrentHashMap<Key, Binding<?>>();

  InstanceGraph() {
    this(null);
  }

  InstanceGraph(@Nullable InstanceGraph parent) {
    this.parent = parent;
  }

  void put(Key key, Binding<?> provider) {
    if (bindings.put(key, provider) != null) {
      throw new IllegalStateException("Duplicate binding for " + key);
    }
  }

  private Binding<?> getBinding(Key key) {
    Binding<?> binding = bindings.get(key);
    if (binding != null) {
      return binding;
    }
    if (parent != null) {
      binding = parent.getBinding(key);
      if (binding != null) {
        return binding;
      }
    }

    Binding<?> newBinding = new JustInTimeBinding(key);
    Binding<?> oldBinding = bindings.putIfAbsent(key, newBinding);
    return oldBinding != null ? oldBinding : newBinding;
  }

  Object getInstance(Key key) {
    return getBinding(key).resolve(this);
  }

  Provider<?> getProvider(final Key key) {
    return new Provider<Object>() {
      @Override public Object get() {
        return getInstance(key);
      }
    };
  }

  Lazy<?> getLazy(Key key) {
    return DoubleCheck.lazy(getProvider(key));
  }
}
