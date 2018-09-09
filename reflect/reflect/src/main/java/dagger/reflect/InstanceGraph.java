package dagger.reflect;

import dagger.Lazy;
import dagger.internal.DoubleCheck;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

final class InstanceGraph {
  private final ConcurrentHashMap<Key, Binding<?>> bindings;
  private final @Nullable InstanceGraph parent;
  final @Nullable Annotation scope;

  private InstanceGraph(
      Map<Key, Binding<?>> bindings,
      @Nullable InstanceGraph parent,
      @Nullable Annotation scope) {
    this.scope = scope;
    this.parent = parent;
    this.bindings = new ConcurrentHashMap<Key, Binding<?>>(bindings);
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

  static final class Builder {
    private final Map<Key, Binding<?>> bindings = new LinkedHashMap<Key, Binding<?>>();
    private @Nullable InstanceGraph parent;
    private @Nullable Annotation scope;

    Builder scope(@Nullable Annotation scope) {
      if (this.scope != null) {
        throw new IllegalStateException("Scope already set");
      }
      this.scope = scope;
      return this;
    }

    Builder parent(@Nullable InstanceGraph parent) {
      if (this.parent != null) {
        throw new IllegalStateException("Parent already set");
      }
      this.parent = parent;
      return this;
    }

    Builder addBinding(Key key, Binding<?> binding) {
      Binding<?> replaced = bindings.put(key, binding);
      if (replaced != null) {
        throw new IllegalStateException(
            "Duplicate binding for " + key + ": " + replaced + " and " + binding);
      }
      return this;
    }

    InstanceGraph build() {
      // TODO traverse scope hierarchy and validate?
      return new InstanceGraph(bindings, parent, scope);
    }
  }
}
