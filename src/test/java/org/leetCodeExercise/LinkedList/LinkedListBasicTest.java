package org.leetCodeExercise.LinkedList;

import org.leetCodeExercise.linkedList.LinkedListBasic;

public class LinkedListBasicTest {
  public static void main(String[] args) {
    LinkedListBasic linkedList = new LinkedListBasic();

    // ===== Test 1: Add elements to end =====
    System.out.println("Test 1: Add to End");
    linkedList.addToEnd(10);
    linkedList.addToEnd(20);
    linkedList.addToEnd(30);
    linkedList.printList(linkedList); // Expected: 10 -> 20 -> 30 ->

    // ===== Test 2: Add element to front =====
    System.out.println("\nTest 2: Add to Front");
    linkedList.addToFront(99);
    linkedList.printList(linkedList); // Expected: 99 -> 10 -> 20 -> 30 ->

    // ===== Test 3: Search existing value =====
    System.out.println("\nTest 3: Search Existing Value");
    System.out.println("Search 20: " + linkedList.search(20)); // Expected: true

    // ===== Test 4: Search non-existing value =====
    System.out.println("\nTest 4: Search Non-Existing Value");
    System.out.println("Search 100: " + linkedList.search(100)); // Expected: false

    // ===== Test 5: Delete head node =====
    System.out.println("\nTest 5: Delete Head");
    linkedList.deleteByValue(99);
    linkedList.printList(linkedList); // Expected: 10 -> 20 -> 30 ->

    // ===== Test 6: Delete middle node =====
    System.out.println("\nTest 6: Delete Middle Node");
    linkedList.deleteByValue(20);
    linkedList.printList(linkedList); // Expected: 10 -> 30 ->

    // ===== Test 7: Delete tail node =====
    System.out.println("\nTest 7: Delete Tail Node");
    linkedList.deleteByValue(30);
    linkedList.printList(linkedList); // Expected: 10 ->

    // ===== Test 8: Delete non-existing value =====
    System.out.println("\nTest 8: Delete Non-Existing Value");
    linkedList.deleteByValue(123); // Should do nothing
    linkedList.printList(linkedList); // Expected: 10 ->

    // ===== Test 9: Delete last remaining element =====
    System.out.println("\nTest 9: Delete Last Element");
    linkedList.deleteByValue(10);
    linkedList.printList(linkedList); // Expected: (empty list)

    // ===== Test 10: Search in empty list =====
    System.out.println("\nTest 10: Search in Empty List");
    System.out.println("Search 5: " + linkedList.search(5)); // Expected: false
  }
}
