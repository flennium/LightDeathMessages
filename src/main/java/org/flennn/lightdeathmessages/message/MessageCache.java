package org.flennn.lightdeathmessages.message;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MessageCache {
    private final String prefix;
    private final Map<String, List<String>> lists;
    private final Map<String, String> strings;

    public MessageCache(String prefix, Map<String, List<String>> lists, Map<String, String> strings) {
        this.prefix = prefix;
        this.lists = Collections.unmodifiableMap(lists);
        this.strings = Collections.unmodifiableMap(strings);
    }

    public static MessageCache empty() {
        return new MessageCache("&8[&cDeath&8]", Collections.emptyMap(), Collections.emptyMap());
    }

    public String getPrefix() {
        return prefix;
    }

    public List<String> getList(String path) {
        return lists.getOrDefault(path, Collections.emptyList());
    }

    public List<String> getDeathCategories() {
        List<String> categories = new ArrayList<>();
        for (String path : lists.keySet()) {
            if (path.startsWith("fallback.")
                    || path.startsWith("player.")
                    || path.startsWith("mob.")
                    || path.startsWith("environment.")
                    || path.startsWith("explosion.")) {
                categories.add(path);
            }
        }
        Collections.sort(categories);
        return categories;
    }

    public String getString(String path, String fallback) {
        return strings.getOrDefault(path, fallback);
    }
}
