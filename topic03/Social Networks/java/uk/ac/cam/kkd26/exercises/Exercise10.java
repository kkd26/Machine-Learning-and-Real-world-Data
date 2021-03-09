package uk.ac.cam.kkd26.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Exercise10 implements IExercise10 {

  /**
   * Adds one directed edge to graph.
   *
   * @param a     start node
   * @param b     end node
   * @param graph graph of neighbours
   */
  private void addEdge(int a, int b, Map<Integer, Set<Integer>> graph) {
    Set<Integer> set = graph.getOrDefault(a, new HashSet<>());
    set.add(b);
    graph.put(a, set);
  }

  @Override
  public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
    Map<Integer, Set<Integer>> graph = new HashMap<>();

    BufferedReader reader = Files.newBufferedReader(graphFile);
    reader.lines().forEach(new Consumer<String>() {
      @Override
      public void accept(String line) {
        String[] nodes = line.split("\\s+");
        int a = Integer.parseInt(nodes[0]);
        int b = Integer.parseInt(nodes[1]);
        addEdge(a, b, graph);
        addEdge(b, a, graph);
      }
    });

    return graph;
  }

  @Override
  public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {
    return graph.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
  }

  /**
   * Runs BFS algorithm from starting node - start and returns furthest node with distance
   *
   * @param start starting node
   * @param graph graph of neighbours
   * @return the furthest node with its distance
   */
  private int[] bfs(int start, Map<Integer, Set<Integer>> graph) {
    Queue<Integer> q = new ArrayDeque<>();
    Set<Integer> visited = new HashSet<>();

    Map<Integer, Integer> d = new HashMap<>();

    q.offer(start);
    visited.add(start);
    d.put(start, 0);

    int last = start;

    while (!q.isEmpty()) {
      int top = q.poll();
      last = top;
      int top_d = d.get(top);
      Set<Integer> neighbours = graph.get(top);
      for (int n : neighbours) {
        if (!visited.contains(n)) {
          visited.add(n);
          q.offer(n);
          d.put(n, top_d + 1);
        }
      }
    }
    return new int[]{last, d.get(last)};
  }

  @Override
  public int getDiameter(Map<Integer, Set<Integer>> graph) {
    int v = (int) graph.keySet().toArray()[0];
    int[] bfs1 = bfs(v, graph);

    return bfs(bfs1[0], graph)[1];
  }
}
