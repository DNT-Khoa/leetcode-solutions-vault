
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Time:  O(M . NlogN) where N = number of logs and M = max length of one log
// Space: O(M · N)

class Solution {
    record WordsByIdentifier(String log, String identifier, String remainingWords) {}

    public String[] reorderLogFiles(String[] logs) {
        List<WordsByIdentifier> letterLogs = new ArrayList<>();
        List<String> digitLogs = new ArrayList<>();
        String[] result = new String[logs.length];

        for (String log : logs) {
            String[] split = log.split(" ", 2);
            String identifier = split[0];
            String remainingWords = split[1];
            if (Character.isDigit(remainingWords.charAt(0))) {
                digitLogs.add(log);
            } else {
                letterLogs.add(new WordsByIdentifier(log, identifier, remainingWords));
            }
        }

        letterLogs.sort(Comparator.comparing(WordsByIdentifier::remainingWords).thenComparing(WordsByIdentifier::identifier));
        int i = 0;
        for (var letterLog : letterLogs) {
            result[i] = letterLog.log();
            i++;
        }
        for (String digitLog : digitLogs) {
            result[i] = digitLog;
            i++;
        }
        return result;
    }
}
