package org.leetCodeExercise.LinkedList;

import org.leetCodeExercise.linkedList.LinkedListBasic;

public class LinkedListBasicTest {
  public static void main(String[] args) {
    LinkedListBasic linkedList = new LinkedListBasic();
    linkedList.addToEnd(10);
    linkedList.addToEnd(20);
    linkedList.addToEnd(30);
    linkedList.addToFront(99);
    linkedList.printList(linkedList);
  }
}
