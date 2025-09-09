package org.leetCodeExercise.practice;

public class MergeKSortedListsPqTest {

  public static void main(String[] args) {

    MergeKSortedListsPq solution = new MergeKSortedListsPq();

    MergeKSortedListsPq.ListNode list1 = solution.new ListNode(1);
    list1.next = solution.new ListNode(4);
    list1.next.next = solution.new ListNode(5);

    MergeKSortedListsPq.ListNode list2 = solution.new ListNode(1);
    list2.next = solution.new ListNode(3);
    list2.next.next = solution.new ListNode(4);

    MergeKSortedListsPq.ListNode list3 = solution.new ListNode(2);
    list3.next = solution.new ListNode(6);

    MergeKSortedListsPq.ListNode[] lists = new MergeKSortedListsPq.ListNode[]{list1, list2, list3};

    MergeKSortedListsPq.ListNode merged = solution.mergeKLists(lists);

    printList(merged);
  }

  public static void printList(MergeKSortedListsPq.ListNode head) {
    MergeKSortedListsPq.ListNode curr = head;
    while (curr != null) {
      System.out.print(curr.val);
      if (curr.next != null) System.out.print(" -> ");
      curr = curr.next;
    }
    System.out.println();
  }
}
