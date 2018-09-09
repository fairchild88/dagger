package dagger.reflect;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.unmodifiableSet;

final class SetBinding<T> extends Binding<Set<T>> {
  private final List<Binding<T>> bindings;
  private int[] requestCounts;

  SetBinding(List<Binding<T>> bindings) {
    this.bindings = new ArrayList<Binding<T>>(bindings);
  }

  @Override protected Request[] initialize(@Nullable Annotation scope) {
    int bindingCount = bindings.size();
    Request[][] bindingRequestArrays = new Request[bindingCount][];
    int[] requestCounts = new int[bindingCount];
    int count = 0;
    for (int i = 0; i < bindingCount; i++) {
      Request[] bindingRequests = bindings.get(i).initialize(scope);
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

  @Override protected Set<T> resolve(Object[] dependencies) {
    int contributionCount = bindings.size();
    Set<T> values = new LinkedHashSet<T>(contributionCount);
    for (int i = 0, start = 0; i < contributionCount; i++) {
      int dependencyCount = requestCounts[i];
      Object[] dependencySubset = new Object[dependencyCount];
      System.arraycopy(dependencies, start, dependencySubset, 0, dependencyCount);

      values.add(bindings.get(i).resolve(dependencySubset));
      start += dependencyCount;
    }
    return unmodifiableSet(values);
  }
}
