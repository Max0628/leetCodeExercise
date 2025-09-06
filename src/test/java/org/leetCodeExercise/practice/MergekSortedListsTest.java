package org.leetCodeExercise.practice;

import static org.leetCodeExercise.practice.MergeKSortedLists.printList;

public class MergekSortedListsTest {
  public static void main(String[] args) {
    MergeKSortedLists mergekSortedLists = new MergeKSortedLists();

    // create list1 = [1,2,4]
    MergeKSortedLists.ListNode list1 = mergekSortedLists.new ListNode(1);
    list1.next = mergekSortedLists.new ListNode(2);
    list1.next.next = mergekSortedLists.new ListNode(4);

    // create list2 = [1,3,4]
    MergeKSortedLists.ListNode list2 = mergekSortedLists.new ListNode(1);
    list2.next = mergekSortedLists.new ListNode(3);
    list2.next.next = mergekSortedLists.new ListNode(4);

    // merge two list.
    MergeKSortedLists.ListNode[] lists = new MergeKSortedLists.ListNode[]{list1, list2};

    // call mergeKLists
    MergeKSortedLists.ListNode result = mergekSortedLists.mergeKLists(lists);

//    printList(list1);  // expected: 1 -> 2 -> 4
//    printList(list2);  // expected: 1 -> 3 -> 4
    printList(result); // check answer here.
  }
}
