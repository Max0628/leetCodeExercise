package org.leetCodeExercise.practice;


import java.util.PriorityQueue;

public class MergeKSortedListsPq {

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


  public ListNode mergeKLists(ListNode[] lists) {
    PriorityQueue<ListNode> pq = new PriorityQueue<>((a, b) -> a.val - b.val);

    // put every headNode to heap.
    for (int i = 0; i < lists.length; i++) {
      if (lists[i] != null) {
        pq.add(lists[i]);
      }
    }

    // create a new linkedList head node.
    ListNode dummy = new ListNode(0);
    ListNode tail = dummy;

    // if heap's peek next element is not null.
    while (pq.peek() != null) {
      ListNode currNode = pq.poll();
      tail.next = currNode;
      currNode = currNode.next;
      if (currNode != null) {
        pq.add(currNode);
      }
      tail = tail.next;

    }

    return dummy.next;
  }
}



