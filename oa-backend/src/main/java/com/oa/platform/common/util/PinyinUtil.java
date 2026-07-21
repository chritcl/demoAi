package com.oa.platform.common.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

/**
 * 拼音工具（基于 pinyin4j），用于通讯录拼音搜索。
 */
public final class PinyinUtil {

    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static {
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    private PinyinUtil() {
    }

    /**
     * 将中文字符串转为小写拼音（连续拼接）。非中文字符保留字母数字并转小写。
     */
    public static String toPinyin(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isChinese(c)) {
                try {
                    String[] arr = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                    if (arr != null && arr.length > 0) {
                        sb.append(arr[0]);
                    }
                } catch (Exception ignored) {
                }
            } else if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 首字母（用于通讯录字母索引）。
     */
    public static String firstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return "#";
        }
        char c = text.charAt(0);
        if (isChinese(c)) {
            try {
                String[] arr = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                if (arr != null && arr.length > 0 && !arr[0].isEmpty()) {
                    return arr[0].substring(0, 1).toUpperCase();
                }
            } catch (Exception ignored) {
            }
            return "#";
        }
        if (Character.isLetter(c)) {
            return String.valueOf(Character.toUpperCase(c));
        }
        return "#";
    }

    private static boolean isChinese(char c) {
        return c >= '一' && c <= '龥';
    }
}
