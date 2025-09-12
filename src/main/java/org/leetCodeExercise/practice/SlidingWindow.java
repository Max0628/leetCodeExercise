package org.leetCodeExercise.practice;

import java.util.HashMap;
import java.util.Map;

public class SlidingWindow {

  public int lengthOfLongestSubstring(String s) {

    if (s.length() == 0 || s == null) {
      return -1;
    }

    int left = 0;
    int max = 0;

    HashMap<Character, Integer> map = new HashMap<>();
    // if right touch string's end, it's traverse all subString of string.
    for (int right = 0; right < s.length(); right++) {
      char c = s.charAt(right);
      // if map already have this key, check it index
      // if the index is smaller than left, represent it is not in current window.
      // if it's bigger than left, it means it's in the window.
      // we need to update the index to index +1's location.
      if (map.containsKey(c) && map.get(c) >= left) {
        left = map.get(c) + 1;
      }
      map.put(c, right);
      max = Math.max(max, right - left + 1);
    }
    return max;
  }
}
