package org.leetCodeExercise.graph;

public class GraphAdjacencyListTest {
  public static void main(String[] args) {
    GraphAdjacencyList graph = new GraphAdjacencyList();
    int numberOfNodes = 3;

    System.out.println("--- 測試開始 ---");

    // 步驟 1: 初始化圖形
    graph.init(numberOfNodes);
    System.out.println("成功初始化一個有 " + numberOfNodes + " 個點的圖形。");
    graph.printGraph();

    // 步驟 2: 增加一條 (1, 2) 的邊
    System.out.println("增加一條 (1, 2) 的邊...");
    graph.addEdge(1, 2);
    graph.printGraph();

    // 步驟 3: 增加一條 (2, 3) 的邊
    System.out.println("增加一條 (2, 3) 的邊...");
    graph.addEdge(2, 3);
    graph.printGraph();

    System.out.println("\n--- 測試結束 ---");
  }
}
