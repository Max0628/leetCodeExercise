package org.leetCodeExercise.graph;

import java.util.ArrayList;
import java.util.List;

public class GraphAdjacencyList {
  List<Integer>[] adjacencyList;

  public void init(int n) {
    adjacencyList = new ArrayList[n + 1];
    for (int i = 1; i <= n; i++) { // start from 1.
      adjacencyList[i] = new ArrayList<>();
    }
  }

  // Undirected graph: add an edge between u and v in both directions
  public void addEdge(int u, int v) {
    adjacencyList[u].add(v);
    adjacencyList[v].add(u);
  }

  // Directed graph: add an edge from u to v only (one direction)
  public void addEdgeV2(int u, int v) {
    adjacencyList[u].add(v);
    adjacencyList[v].add(u);
  }

  public void printGraph() {
    for (int i = 1; i < adjacencyList.length; i++) {
      System.out.print(i + " -> ");
      for (int neighbor : adjacencyList[i]) {
        System.out.print(neighbor + " ");
      }
      System.out.println();
    }
  }
}
