package com.agileboot.common.enums.common;

import com.agileboot.common.enums.DictionaryEnum;
import com.agileboot.common.enums.dictionary.CssTag;
import com.agileboot.common.enums.dictionary.Dictionary;

@Dictionary(name = "common.alarmLevel")
public enum AlarmLevelEnum implements DictionaryEnum<String> {

    RED("red", "红色", CssTag.DANGER),
    YELLOW("yellow", "黄色", CssTag.WARNING);

    private final String value;
    private final String description;
    private final String cssTag;

    AlarmLevelEnum(String value, String description, String cssTag) {
        this.value = value;
        this.description = description;
        this.cssTag = cssTag;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String cssTag() {
        return cssTag;
    }

}
