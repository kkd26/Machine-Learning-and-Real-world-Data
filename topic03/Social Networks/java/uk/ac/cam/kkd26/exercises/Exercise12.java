package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise12;

import java.util.*;

public class Exercise12 implements IExercise12 {
  @Override
  public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents) {
    List<Set<Integer>> components = getComponents(graph);
    while (components.size() < minimumComponents && getNumberOfEdges(graph) > 0) {
      Map<Integer, Map<Integer, Double>> edgeBetweenness = getEdgeBetweenness(graph);

      Set<Integer> V = graph.keySet();

      Map<Integer, Set<Integer>> toRemove = new HashMap<>();

      double max = -1.0;
      for (int v : V) {
        for (int w : V) {
          Double edge = edgeBetweenness.get(v).get(w);
          if (edge >= max) {
            if (edge > max) toRemove = new HashMap<>();
            Set<Integer> s = toRemove.getOrDefault(v, new HashSet<>());
            s.add(w);
            toRemove.put(v, s);
            max = edge;
          }
        }
      }

      for (int v : toRemove.keySet()) {
        for (int w : toRemove.get(v)) {
          graph.get(v).remove(w);
        }
      }

      components = getComponents(graph);
    }

    return components;
  }

  @Override
  public int getNumberOfEdges(Map<Integer, Set<Integer>> graph) {
    Set<Integer> V = graph.keySet();

    return V.stream().mapToInt(v -> v).map(v -> graph.get(v).size()).sum() / 2;
  }

  private Set<Integer> dfs(int v, Map<Integer, Set<Integer>> graph, Map<Integer, Integer> d) {
    Set<Integer> component = new HashSet<>();

    Stack<Integer> s = new Stack<>();
    s.add(v);
    d.put(v, 0);

    while (!s.empty()) {
      int top = s.pop();
      component.add(top);
      int dist_top = d.get(top);
      for (int w : graph.get(top)) {
        if (d.get(w) < 0) {
          d.put(w, dist_top + 1);
          s.add(w);
        }
      }
    }

    return component;
  }

  @Override
  public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph) {
    Set<Integer> V = graph.keySet();
    Map<Integer, Integer> d = new HashMap<>();

    for (int v : V) d.put(v, -1);

    List<Set<Integer>> components = new ArrayList<>();

    for (int v : V) {
      if (d.get(v) < 0) {
        Set<Integer> component = dfs(v, graph, d);
        components.add(component);
      }
    }
    return components;
  }

  private static void edgeAccumulation(Map<Integer, Set<Integer>> P, Stack<Integer> S, Map<Integer, Integer> sigma, Map<Integer, Double> delta, Map<Integer, Map<Integer, Double>> C) {
    for (int v : S) {
      delta.put(v, .0);
    }

    while (!S.isEmpty()) {
      int w = S.pop();
      for (int v : P.get(w)) {
        double c = 1.0 * sigma.get(v) * (1.0 + delta.get(w)) / sigma.get(w);
        Map<Integer, Double> temp = C.get(v);
        temp.put(w, temp.get(w) + c);
        C.put(v, temp);
        delta.put(v, delta.get(v) + c);
      }
    }
  }

  private static void edgeBetweenness(int s, Map<Integer, Set<Integer>> graph, Map<Integer, Map<Integer, Double>> C) {
    Map<Integer, Set<Integer>> P = new HashMap<>(); // predecessors on the shortest path
    Stack<Integer> S = new Stack<>();               // visited order
    Map<Integer, Integer> sigma = new HashMap<>();  // number of shortest paths from source
    Map<Integer, Double> delta = new HashMap<>();   // dependency on source

    Exercise11.singlePointShortestPaths(s, graph, P, S, sigma);
    edgeAccumulation(P, S, sigma, delta, C);
  }

  private static Map<Integer, Map<Integer, Double>> betweenness(Map<Integer, Set<Integer>> graph) {
    Map<Integer, Map<Integer, Double>> C = new HashMap<>();
    Set<Integer> V = graph.keySet();
    for (int v : V) {
      C.put(v, new HashMap<>());
      Map<Integer, Double> temp = C.get(v);
      for (int w : V) {
        temp.put(w, .0);
      }
      C.put(v, temp);
    }

    for (int s : graph.keySet()) {
      edgeBetweenness(s, graph, C);
    }

    return C;
  }

  @Override
  public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph) {
    return betweenness(graph);
  }
}
