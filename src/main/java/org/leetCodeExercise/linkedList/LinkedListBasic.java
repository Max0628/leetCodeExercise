package org.leetCodeExercise.linkedList;

//Singly Linked List
public class LinkedListBasic {

  class Node {
    int val;
    Node next;

    // constructure
    Node(int val) {
      this.val = val;
      this.next = null;
    }
  }

  // head is not a "Node" it's just an attachment of point, like the head of a train , but not the first train carriage
  private Node head;

  // while initialize an  LinkedListBasic Object, it will automatically generate a head variable.
  public LinkedListBasic() {
    this.head = null;
  }

  // add new element to the front of linkedList.
  public void addToFront(int val) {
    Node newNode = new Node(val);

    // reset "head"
    // step1: set the original head to newNode.next
    newNode.next = head;

    // step2: set newNode you just create to the head variable.
    head = newNode;
  }

  // add new element to the end of linkedList
  public void addToEnd(int val) {
    Node newNode = new Node(val);

    // if the list is empty, make the new node the headl.
    if (head == null) {
      head = newNode;
      return;
    }

    // start from the head and use "current" to traverse the list.
    Node current = head;

    // move the "current" until it reaches the last node(current.next==null)
    while (current.next != null) {
      current = current.next;
    }

    // link the last node to the noew node.
    current.next = newNode;
  }

  // print the list.
  public void printList(LinkedListBasic linkedList) {
    Node current = head;
    while (current != null) {
      System.out.println(current.val + "->");
      current = current.next;
    }
  }

}
