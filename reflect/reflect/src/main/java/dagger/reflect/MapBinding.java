package dagger.reflect;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.unmodifiableMap;

final class MapBinding<K, V> extends Binding<Map<K, V>> {
  private final List<Map.Entry<K, Binding<V>>> bindingEntries;
  private int[] requestCounts;

  MapBinding(Map<K, Binding<V>> bindings) {
    bindingEntries = new ArrayList<Map.Entry<K, Binding<V>>>(bindings.entrySet());
  }

  @Override protected Request[] initialize(@Nullable Annotation scope) {
    int bindingCount = bindingEntries.size();
    Request[][] bindingRequestArrays = new Request[bindingCount][];
    int[] requestCounts = new int[bindingCount];
    int count = 0;
    for (int i = 0; i < bindingCount; i++) {
      Map.Entry<K, Binding<V>> bindingEntry = bindingEntries.get(i);
      Request[] bindingRequests = bindingEntry.getValue().initialize(scope);
      bindingRequestArrays[i] = bindingRequests;
      requestCounts[i] = bindingRequests.length;
      count += bindingRequests.length;
    }

    Request[] requests = new Request[count];
    for (int i = 0, start = 0; i < bindingCount; i++) {
      Request[] bindingRequests = bindingRequestArrays[i];
      System.arraycopy(bindingRequests, 0, requests, start, bindingRequests.length);
      start += bindingRequests.length;
    }

    this.requestCounts = requestCounts;
    return requests;
  }

  @Override protected Map<K, V> resolve(Object[] dependencies) {
    int contributionCount = bindingEntries.size();
    Map<K, V> values = new LinkedHashMap<K, V>(contributionCount);
    for (int i = 0, start = 0; i < contributionCount; i++) {
      int dependencyCount = requestCounts[i];
      Object[] dependencySubset = new Object[dependencyCount];
      System.arraycopy(dependencies, start, dependencySubset, 0, dependencyCount);

      Map.Entry<K, Binding<V>> bindingEntry = bindingEntries.get(i);
      values.put(bindingEntry.getKey(), bindingEntry.getValue().resolve(dependencySubset));
      start += dependencyCount;
    }
    return unmodifiableMap(values);
  }
}
