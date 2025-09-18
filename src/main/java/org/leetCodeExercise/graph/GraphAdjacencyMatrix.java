package org.leetCodeExercise.graph;

public class GraphAdjacencyMatrix {
  int[][] adjacencyMatrix;

  public void init(int n) {
    adjacencyMatrix = new int[n + 1][n + 1];
  }

  public void addEdge(int u, int v) {
    //Undirected graph
    adjacencyMatrix[u][v] = 1;
    adjacencyMatrix[v][u] = 1;

    //directed graph
    adjacencyMatrix[u][v] = 1;
  }
}

