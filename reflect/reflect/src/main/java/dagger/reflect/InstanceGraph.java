package dagger.reflect;

import dagger.Lazy;
import dagger.internal.DoubleCheck;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

final class InstanceGraph {
  private final ConcurrentHashMap<Key, Binding<?>> bindings;
  private final @Nullable InstanceGraph parent;
  final @Nullable Annotation scope;

  private InstanceGraph(
      ConcurrentHashMap<Key, Binding<?>> bindings,
      @Nullable InstanceGraph parent,
      @Nullable Annotation scope) {
    this.scope = scope;
    this.parent = parent;
    this.bindings = bindings;
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
    private final Map<Key, List<Binding<Object>>> setBindings =
        new LinkedHashMap<Key, List<Binding<Object>>>();
    private final Map<Key, Map<Object, Binding<Object>>> mapBindings =
        new LinkedHashMap<Key, Map<Object, Binding<Object>>>();
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

    Builder addBinding(Key key, Binding<Object> binding) {
      checkedPut(bindings, key, binding);
      return this;
    }

    Builder addSetBinding(Key key, Binding<Object> binding) {
      // TODO validate key is a Set?

      List<Binding<Object>> bindings = setBindings.get(key);
      if (bindings == null) {
        bindings = new ArrayList<Binding<Object>>();
        setBindings.put(key, bindings);
      }
      bindings.add(binding);
      return this;
    }

    Builder addMapBinding(Key key, Object mapKey, Binding<Object> binding) {
      // TODO validate key is a Map?

      Map<Object, Binding<Object>> bindings = mapBindings.get(key);
      if (bindings == null) {
        bindings = new LinkedHashMap<Object, Binding<Object>>();
        mapBindings.put(key, bindings);
      }
      bindings.put(mapKey, binding);
      return this;
    }

    InstanceGraph build() {
      if (scope != null) {
        InstanceGraph check = parent;
        while (check != null) {
          if (parent.scope == null) {
            throw new IllegalStateException("Scoped graph cannot depend on an unscoped one");
          }
          if (parent.scope.equals(scope)) {
            throw new IllegalStateException("Scope " + scope + " found in parent graph chain");
          }
          check = check.parent;
        }
      }

      ConcurrentHashMap<Key, Binding<?>> bindings =
          new ConcurrentHashMap<Key, Binding<?>>(this.bindings);

      // TODO get set bindings from parents and merge
      for (Map.Entry<Key, List<Binding<Object>>> entry : setBindings.entrySet()) {
        checkedPut(bindings, entry.getKey(), new SetBinding<Object>(entry.getValue()));
      }

      // TODO get map bindings from parents and merge
      for (Map.Entry<Key, Map<Object, Binding<Object>>> entry : mapBindings.entrySet()) {
        checkedPut(bindings, entry.getKey(), new MapBinding<Object, Object>(entry.getValue()));
      }

      return new InstanceGraph(bindings, parent, scope);
    }

    private static <T> void checkedPut(Map<Key, T> bindings, Key key, T binding) {
      T replaced = bindings.put(key, binding);
      if (replaced != null) {
        throw new IllegalStateException(
            "Duplicate value for key " + key + ": " + replaced + " and " + binding);
      }
    }
  }
}
