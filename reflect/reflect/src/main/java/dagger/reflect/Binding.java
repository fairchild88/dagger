package dagger.reflect;

import java.lang.annotation.Annotation;
import org.jetbrains.annotations.Nullable;

abstract class Binding<T> {
  private Request[] requests;

  protected abstract Request[] initialize(@Nullable Annotation scope);

  protected abstract T resolve(Object[] dependencies);

  final T resolve(InstanceGraph graph) {
    Request[] requests = this.requests;
    if (requests == null) {
      synchronized (this) {
        requests = this.requests;
        if (requests == null) {
          requests = initialize(graph.scope);
          if (requests == null) {
            throw new IllegalStateException("Binding " + this + " returned null from initialize()");
          }
          this.requests = requests;
        }
      }
    }

    int requestCount = requests.length;
    Object[] dependencies = new Object[requestCount];
    for (int i = 0; i < requestCount; i++) {
      dependencies[i] = requests[i].resolve(graph);
    }
    return resolve(dependencies);
  }
}
