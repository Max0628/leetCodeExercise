package org.leetCodeExercise.linkedList;

//Singly Linked List
public class LinkedListBasic {

  class Node {
    // current node value
    int val;
    // next node's object
    Node next;

    // constructure
    Node(int val) {
      // when create a Node, put value in Node.
      this.val = val;
      // initialize
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

  /**
   * Delete a node value from the linked list.
   * Time Complexity: O(n) worst case need to traverse the entire list.
   * Space Complexity: O(1) only constant extra space is used.
   *
   * @param val
   */
  public void deleteByValue(int val) {
    if (head == null) {
      // list is empty, noting need to delete.
      return;
    }

    // case 1: the head is node to delete.
    if (head.val == val) {

      // make the original head a "orphan node", GC will clean it.
      head = head.next;
      return;
    }

    //case 2: search the node before the target.
    Node current = head;
    while (current.next != null && current.next.val != val) {
      current = current.next;
    }
    // exit the while loop means get the value.

    //case 3: if found,skip the target node.
    // if current.next(target) is not null, point the target to the next next element.
    if (current.next != null) {
      current.next = current.next.next;
    }
  }


  /**
   * Search a node by value in the linked list.
   * Time Complexity: O(n) worst case need to traverse the entire list.
   * Space Complexity: O(1) only a constant amout of extra space is used. "Node current = head;"
   *
   * @param val
   *
   * @return ture if the value is existed in the list.
   */
  public boolean search(int val) {
    Node current = head;

    // if head is not empty
    while (current != null) {

      // match.
      if (current.val == val) {
        return true;
      }

      // move to next node.
      current = current.next;
    }
    // the list is empty.
    return false;
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
