// Time:  O(N.4^N)
// Space: O(N)

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Solution {
    public List<String> letterCombinations(String digits) {
        Map<Character, String> numToLetters = Map.of(
            '2',
            "abc",
            '3',
            "def",
            '4',
            "ghi",
            '5',
            "jkl",
            '6',
            "mno",
            '7',
            "pqrs",
            '8',
            "tuv",
            '9',
            "wxyz"
        );

        List<String> result = new ArrayList<>();
        explore(digits, 0, new StringBuilder(), result, numToLetters);
        return result;
    }

    void explore(String digits, int currentDigit, StringBuilder current, List<String> result, Map<Character, String> numToLetters) {
        if (current.length() == digits.length()) {
            result.add(current.toString());
            return;
        } 

        String letters = numToLetters.get(digits.charAt(currentDigit));
        for (char c : letters.toCharArray()) {
            current.append(c);
            explore(digits, currentDigit + 1, current, result, numToLetters);
            current.deleteCharAt(current.length() - 1);
        }
    }
}
