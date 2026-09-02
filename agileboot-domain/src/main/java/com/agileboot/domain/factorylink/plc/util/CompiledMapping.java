package com.agileboot.domain.factorylink.plc.util;

import com.agileboot.domain.factorylink.plc.entity.FieldMappingEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * field_mapping 预编译后的条目：Pattern + 占位符名列表。
 * 启动时由 compile() 生成，避免每次匹配都重编译。
 */
public final class CompiledMapping {
    final FieldMappingEntity mapping;
    final Pattern pattern; // null 表示 EXACT（无占位符）
    final List<String> placeholderNames;

    public CompiledMapping(FieldMappingEntity mapping, Pattern pattern, List<String> placeholderNames) {
        this.mapping = mapping;
        this.pattern = pattern;
        this.placeholderNames = placeholderNames;
    }

    public FieldMappingEntity getMapping() { return mapping; }
    public Pattern getPattern() { return pattern; }
    public List<String> getPlaceholderNames() { return placeholderNames; }

    public static List<CompiledMapping> compile(List<FieldMappingEntity> mappings) {
        List<CompiledMapping> out = new ArrayList<>(mappings.size());
        for (FieldMappingEntity m : mappings) {
            List<String> names = new ArrayList<>();
            Matcher m0 = FieldMatchingEngine.PLACEHOLDER_PATTERN.matcher(m.getMatchPattern());
            StringBuffer sb = new StringBuffer();
            while (m0.find()) {
                String name = m0.group(1);
                names.add(name);
                String regex = FieldMatchingEngine.PLACEHOLDER_REGEX.getOrDefault(name, "(?<" + name + ">[^{}]+)");
                m0.appendReplacement(sb, Matcher.quoteReplacement(regex));
            }
            m0.appendTail(sb);
            Pattern p = names.isEmpty() ? null : Pattern.compile("^" + sb + "$");
            out.add(new CompiledMapping(m, p, names));
        }
        return out;
    }
}
