package utils;

import java.util.HashMap;
import java.util.Map;

public class ParseUtils {
    public static Map<String, String> parseKeyValuePairs(String line) {
        Map<String, String> map = new HashMap<>();
        String[] keyValuePairs = line.split(";");
        for (String kv : keyValuePairs) {
            String[] parts = kv.split("=", 2);
            if (parts.length == 2) {
                map.put(parts[0].trim().toLowerCase(), parts[1].trim());
            }
        }
        return map;
    }
}