package com.kaoyan.platform.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Sensitive word filter utility.
 * Uses a DFA (Deterministic Finite Automaton) for efficient multi-pattern matching.
 */
public class SensitiveWordFilter {

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>();
    private static final Map<Character, Object> DFA_MAP = new HashMap<>();
    private static final String REPLACEMENT = "***";

    static {
        // 广告类
        SENSITIVE_WORDS.add("广告");
        SENSITIVE_WORDS.add("推广");
        SENSITIVE_WORDS.add("代购");
        SENSITIVE_WORDS.add("刷单");
        SENSITIVE_WORDS.add("微商");
        SENSITIVE_WORDS.add("营销");
        SENSITIVE_WORDS.add("加微信");
        SENSITIVE_WORDS.add("扫码");
        SENSITIVE_WORDS.add("兼职");
        SENSITIVE_WORDS.add("代理");

        // 赌博类
        SENSITIVE_WORDS.add("赌博");
        SENSITIVE_WORDS.add("博彩");
        SENSITIVE_WORDS.add("赌场");
        SENSITIVE_WORDS.add("彩票");
        SENSITIVE_WORDS.add("六合彩");
        SENSITIVE_WORDS.add("时时彩");
        SENSITIVE_WORDS.add("下注");

        // 色情类
        SENSITIVE_WORDS.add("色情");
        SENSITIVE_WORDS.add("黄色");
        SENSITIVE_WORDS.add("裸聊");
        SENSITIVE_WORDS.add("约炮");
        SENSITIVE_WORDS.add("一夜情");
        SENSITIVE_WORDS.add("成人");
        SENSITIVE_WORDS.add("淫秽");

        // 暴力类
        SENSITIVE_WORDS.add("暴力");
        SENSITIVE_WORDS.add("杀人");
        SENSITIVE_WORDS.add("枪支");
        SENSITIVE_WORDS.add("弹药");
        SENSITIVE_WORDS.add("恐怖");
        SENSITIVE_WORDS.add("爆炸");

        // 政治敏感类
        SENSITIVE_WORDS.add("法轮功");
        SENSITIVE_WORDS.add("六四");
        SENSITIVE_WORDS.add("台独");
        SENSITIVE_WORDS.add("藏独");
        SENSITIVE_WORDS.add("疆独");
        SENSITIVE_WORDS.add("港独");
        SENSITIVE_WORDS.add("反党");
        SENSITIVE_WORDS.add("反共");
        SENSITIVE_WORDS.add("颠覆");

        // 违法类
        SENSITIVE_WORDS.add("毒品");
        SENSITIVE_WORDS.add("贩卖");
        SENSITIVE_WORDS.add("走私");
        SENSITIVE_WORDS.add("洗钱");
        SENSITIVE_WORDS.add("诈骗");
        SENSITIVE_WORDS.add("传销");
        SENSITIVE_WORDS.add("高利贷");

        // 其他违规
        SENSITIVE_WORDS.add("人肉搜索");
        SENSITIVE_WORDS.add("黑客");
        SENSITIVE_WORDS.add("翻墙");
        SENSITIVE_WORDS.add("VPN");

        // Build DFA
        buildDFA();
    }

    @SuppressWarnings("unchecked")
    private static void buildDFA() {
        for (String word : SENSITIVE_WORDS) {
            Map<Character, Object> currentMap = DFA_MAP;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Map<Character, Object> childMap = (Map<Character, Object>) currentMap.get(c);
                if (childMap == null) {
                    childMap = new HashMap<>();
                    currentMap.put(c, childMap);
                }
                currentMap = childMap;
            }
            currentMap.put('\0', Boolean.TRUE); // end marker
        }
    }

    /**
     * Check whether the given text contains any sensitive word.
     *
     * @param text the text to check, may be null
     * @return true if sensitive content is found
     */
    public static boolean containsSensitive(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int len = text.length();
        for (int i = 0; i < len; i++) {
            int matchLen = checkPrefix(text, i);
            if (matchLen > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Filter sensitive words in the given text by replacing them with "***".
     *
     * @param text the text to filter, may be null
     * @return filtered text with sensitive words replaced
     */
    public static String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        int i = 0;
        while (i < len) {
            int matchLen = checkPrefix(text, i);
            if (matchLen > 0) {
                sb.append(REPLACEMENT);
                i += matchLen;
            } else {
                sb.append(text.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * Check if the substring starting at position {@code start} matches any
     * sensitive word in the DFA.
     *
     * @param text  the full text
     * @param start the starting index
     * @return the length of the matched word, or 0 if no match
     */
    @SuppressWarnings("unchecked")
    private static int checkPrefix(String text, int start) {
        Map<Character, Object> currentMap = DFA_MAP;
        int matchLen = 0;
        for (int i = start; i < text.length(); i++) {
            Map<Character, Object> childMap = (Map<Character, Object>) currentMap.get(text.charAt(i));
            if (childMap == null) {
                break;
            }
            matchLen++;
            if (childMap.containsKey('\0')) {
                return matchLen;
            }
            currentMap = childMap;
        }
        return 0;
    }
}
