
import java.util.regex.Pattern;

// Time:  O(N) where N is the number of characters in queryIP
// Space: O(1)

class Solution {
    static final String X_IPV4 = "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
    static final Pattern PATTERN_IPV4 = Pattern.compile("(" + X_IPV4 + "\\.){3}" + X_IPV4);
    static final String X_IPV6 = "([0-9a-fA-F]{1,4})";
    static final Pattern PATTERN_IPV6 = Pattern.compile("(" + X_IPV6 + ":){7}" + X_IPV6);

    public String validIPAddress(String queryIP) {
        if (PATTERN_IPV4.matcher(queryIP).matches()) {
            return "IPv4";
        }

        if (PATTERN_IPV6.matcher(queryIP).matches()) {
            return "IPv6";
        }

        return "Neither";
    }
}
