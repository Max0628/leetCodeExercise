package org.leetCodeExercise.practice;

public class MergeKSortedLists {
  // singly linked list.
  public class ListNode {
    int val;
    ListNode next;

    // empty constructor.
    ListNode() {
    }

    // Overload Constructor: initialize node with value only.
    ListNode(int val) {
      this.val = val;
    }

    // Overload Constructor: for Node element.
    ListNode(int val, ListNode next) {
      this.val = val;
      this.next = next;
    }
  }


  /**
   * Description:
   * 1. Separate k into two element a group.
   * 2. Merge them together and make them sorted.
   * 3. Lt's seems only need to do the "conquer" part.
   * @param lists
   * @return
   */
  public ListNode mergeKLists(ListNode[] lists) {

    //edge case.
    if (lists == null || lists.length == 0) {
      return null;
    }

    // use divide & conquer to merge all lists.
    // list is a Node array, start from 0 to lists.length-1.
    return mergeKListsRecursively(lists, 0, lists.length - 1);// if lists.length = 7, left = 0, right = 6.
  }

  /**
   * Recursively merges lists in the range [left, right].
   * @param lists Lists Array of ListNode heads.
   * @param left  left Start index.
   * @param right right End index.
   * @return Head of the merged sorted linked list for this range.
   */
  public ListNode mergeKListsRecursively(ListNode[] lists, int left, int right) {//left = 0, right = 6
    try {

      // base case: only one list is in range.
      if (left == right) {
        return lists[left];// because it's right close, lists[left] is available.
      }

      // find midpoint of this range.
      int mid = left + (right - left) / 2;//mid = 3

      //recursively merge the left & right half.
      ListNode l1 = mergeKListsRecursively(lists, left, mid);//left = 0,right = 3
      ListNode l2 = mergeKListsRecursively(lists, mid+1, right);//left = 4, right = 6

      // until hit the base case and start return.
      return sortTwoLinkedListLogic(l1, l2);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ListNode(0);
  }

  /**
   * Do the merging part here.
   *
   * @param node1
   * @param node2
   *
   * @return
   */
  public ListNode sortTwoLinkedListLogic(ListNode node1, ListNode node2) {
    // edge case.
    if (node1 == null) {
      return node2;
    }
    if (node2 == null) {
      return node1;
    }

    ListNode dummy = new ListNode(0);
    ListNode tail = dummy; // pointer tail to dummy in the beginning.
    ListNode curr1 = node1;
    ListNode curr2 = node2;

    // if one of the list is run out , exit loop.
    while (curr1 != null && curr2 != null) {
      // compare first element to decided which list will be head.
      if (curr1.val <= curr2.val) {
        // at this moment, tail is point to dommy,so i can use tail as dommy.
        tail.next = curr1;
        // move current to next node.
        curr1 = curr1.next;
      } else {
        tail.next = curr2;
        curr2 = curr2.next;
      }
      // after do our job, move tail backward.
      tail = tail.next;
    }

    if (curr1 != null) {
      tail.next = curr1;
    }
    if (curr2 != null) {
      tail.next = curr2;
    }
    return dummy.next;
  }

  // print linked list methods.
  // send head in to methods, and start traverse to linked List.
  public static void printList(ListNode head) {

    // assign head to current variable.
    ListNode curr = head;

    // if current is not null.
    while (curr != null) {
      System.out.print(curr.val);
      if (curr.next != null) System.out.print(" -> ");
      // move to next node.
      curr = curr.next;
    }
  }
}
