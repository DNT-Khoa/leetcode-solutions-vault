
import java.util.regex.Pattern;

// Time:  O(N) where N is the number of characters in queryIP
// Space: O(N)

class Solution {

    public String validIPAddress(String queryIP) {
        if (isIpV4(queryIP)) {
            return "IPv4";
        }

        if (isIpV6(queryIP)) {
            return "IPv6";
        }

        return "Neither";
    }

    private boolean isIpV4(String queryIP) {
        Pattern pattern = Pattern.compile("0|[1-9][0-9]{0,2}");
        String[] splitted = queryIP.split("\\.", -1);
        if (splitted.length != 4) return false;

        for (String x : splitted) {
            if (!pattern.matcher(x).matches() || Integer.parseInt(x) > 255) return false;
        }

        return true;
    }

    private boolean isIpV6(String queryIP) {
        Pattern pattern = Pattern.compile("[0-9a-fA-F]{1,4}");
        String[] splitted = queryIP.split(":", -1);
        if (splitted.length != 8) return false;

        for (String x : splitted) {
            if (!pattern.matcher(x).matches()) return false;
        }
        return true;
    }
}
