package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise11;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise11 implements IExercise11 {

  /**
   * Calculates Brandes' number of shortest paths for a source
   *
   * @param source source node
   * @param graph  graph of neighbours
   * @param P      predecessors on the shortest path
   * @param S      visited order
   * @param sigma  number of shortest paths from source
   */
  public static void singlePointShortestPaths(
    int source,
    Map<Integer, Set<Integer>> graph,
    Map<Integer, Set<Integer>> P,
    Stack<Integer> S,
    Map<Integer, Integer> sigma) {

    Queue<Integer> Q = new ArrayDeque<>();          // BFS queue
    Map<Integer, Integer> d = new HashMap<>();      // distance from source

    for (int v : graph.keySet()) {
      P.put(v, new HashSet<>());
      sigma.put(v, 0);
      d.put(v, -1);
    }

    Q.offer(source);
    sigma.put(source, 1);
    d.put(source, 0);

    while (!Q.isEmpty()) {
      int v = Q.poll();
      S.add(v);
      int dist_v = d.get(v);
      int sigma_v = sigma.get(v);
      Set<Integer> neighbours = graph.get(v);
      for (int w : neighbours) {
        if (d.get(w) < 0) {
          Q.offer(w);
          d.put(w, dist_v + 1);
        }
        if (d.get(w) == dist_v + 1) {
          sigma.put(w, sigma.get(w) + sigma_v);
          Set<Integer> s = P.get(w);
          s.add(v);
          P.put(w, s);
        }
      }
    }
  }

  /**
   * An accumulator for single point betweenness
   * @param P      predecessors on the shortest path
   * @param S      visited order
   * @param sigma  number of shortest paths from source
   * @param delta  dependency on source
   */
  private static void singlePointAccumulation(Map<Integer, Set<Integer>> P, Stack<Integer> S, Map<Integer, Integer> sigma, Map<Integer, Double> delta) {
    for (int v : S) {
      delta.put(v, .0);
    }

    while (!S.isEmpty()) {
      int w = S.pop();
      for (int v : P.get(w)) {
        double delta_v = delta.get(v);
        delta_v += 1.0 * sigma.get(v) * (1.0 + delta.get(w)) / sigma.get(w);
        delta.put(v, delta_v);
      }
    }
  }

  private static Map<Integer, Double> singlePointBetweenness(int s, Map<Integer, Set<Integer>> graph) {
    Map<Integer, Double> C = new HashMap<>();

    for (int v : graph.keySet()) C.put(v, .0);

    Map<Integer, Set<Integer>> P = new HashMap<>(); // predecessors on the shortest path
    Stack<Integer> S = new Stack<>();               // visited order
    Map<Integer, Integer> sigma = new HashMap<>();  // number of shortest paths from source
    Map<Integer, Double> delta = new HashMap<>();   // dependency on source

    singlePointShortestPaths(s, graph, P, S, sigma);
    singlePointAccumulation(P, S, sigma, delta);

    for (int w : graph.keySet()) {
      if (w != s) C.put(w, C.get(w) + delta.get(w));
    }

    return C;
  }

  private static Map<Integer, Double> betweenness(Map<Integer, Set<Integer>> graph) {
    Map<Integer, Double> C = new HashMap<>();

    for (int v : graph.keySet()) C.put(v, .0);

    for (int s : graph.keySet()) {
      Map<Integer, Double> currentC = singlePointBetweenness(s, graph);
      C.replaceAll((v, val) -> val + currentC.get(v));
    }

    C.replaceAll((v, value) -> value / 2);

    return C;
  }

  @Override
  public Map<Integer, Double> getNodeBetweenness(Path graphFile) throws IOException {
    Map<Integer, Set<Integer>> graph = (new Exercise10()).loadGraph(graphFile);

    return betweenness(graph);
  }
}
