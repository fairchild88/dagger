package dagger.reflect;

abstract class Binding<T> {
  private Request[] requests;

  protected abstract Request[] initialize();

  protected abstract T resolve(Object[] dependencies);

  final T resolve(InstanceGraph graph) {
    Request[] requests = this.requests;
    if (requests == null) {
      synchronized (this) {
        requests = this.requests;
        if (requests == null) {
          requests = initialize();
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
