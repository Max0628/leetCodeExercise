package org.leetCodeExercise.practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class isValidStack {


  /**
   * Description: use stack to solve this issue.
   * Time Complexity: O(N)
   * Space Complexity: O(N)
   * @param s
   * @return
   */
  public boolean isValid(String s) {
    boolean isVaild = false;
    //{[()]}
    //{}[]()
    //([)]

    if (s == null || s.length() == 0 || (s.length() % 2) != 0) {
      return false;
    }

    LinkedList<Character> leftBracketOnlyStack = new LinkedList<>();
    for (int i = 0; i < s.length(); i++) {
      //current traverse element.
      char curr = s.charAt(i);

      // add only leftBracket into an array.
      if (curr == '{' || curr == '[' || curr == '(') {
        leftBracketOnlyStack.add(curr);
      }

      // meet right bracket, check if stack have matched left bracket.
      // if yes, remove the left bracket.
      if (curr == '}' || curr == ']' || curr == ')') {
        //current last index of
        if (leftBracketOnlyStack == null || leftBracketOnlyStack.isEmpty()) {
          return false;
        }
        Character currentLastElement = leftBracketOnlyStack.get(leftBracketOnlyStack.size() - 1);

        if (curr == '}') {
          if (currentLastElement == '{') {
            leftBracketOnlyStack.removeLast();
          } else {
            return false;
          }
          ;
        }

        if (curr == ']') {
          if (currentLastElement == '[') {
            leftBracketOnlyStack.removeLast();
          } else {
            return false;
          }
          ;
        }

        if (curr == ')') {
          if (currentLastElement == '(') {
            leftBracketOnlyStack.removeLast();
          } else {
            return false;
          }
          ;
        }
      }
    }
    return leftBracketOnlyStack.size() == 0 ? true : false;
  }
}

// 1. 宣告一個array專門用來存還沒配對到的左瓜號
// 2. 用for loop traverse 整個string
// 3. 跑到右括號的時候，去檢查每個右括號 peek 一下左括號陣列，如果相等就pop,
// 4. 如我最後順利清空左括號陣列，且迭帶完畢右括號陣列，就return true ,否則return false