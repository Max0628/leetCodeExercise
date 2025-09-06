package org.leetCodeExercise.linkedList;

public class DoublyLinkedList {

  class Node {
    int val;
    Node prev;
    Node next;

    Node(int val) {
      this.val = val;
      this.prev = null;
      this.next = null;
    }
  }

  private Node head;
  private Node tail;

  // initialize class generate head & tail.
  public void DoublyLinkedList() {
    this.head = null;
    this.tail = null;
  }

  // add new node to the front.
  public void addToFront(int val) {
    Node newNode = new Node(val);
    if (head == null) {
      // point both head & tail to newNode.
      head = newNode;
      tail = newNode;
    } else {

      // assign old head to newNode's next element.
      newNode.next = head;

      // assign newNode to the previous
      head.prev = newNode;
      head = newNode;
    }
  }

  // add new node to the end.
  public void addToEnd(int val) {
    Node newNode = new Node(val);
    if (tail == null) {
      head = newNode;
      tail = newNode;
    } else {
      tail.next = newNode;
      tail.prev = tail;
      tail = newNode;
    }
  }

  public void deleteByValue(int val) {

    Node current = head;
    // target is the first element.
    while (current != null) {

    }
  }
}
