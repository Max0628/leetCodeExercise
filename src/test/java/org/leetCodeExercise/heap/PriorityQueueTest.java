package org.leetCodeExercise.heap;

public class PriorityQueueTest {
  public static void main(String[] args) {
    PriorityQueue pq = new PriorityQueue(10);

    pq.insert(5);
    pq.insert(3);
    pq.insert(8);
    pq.insert(1);

    System.out.println(pq.peek()); // 1
    System.out.println(pq.poll()); // 1
    System.out.println(pq.poll()); // 3
    System.out.println(pq.poll()); // 5
    System.out.println(pq.poll()); // 8
  }
}
