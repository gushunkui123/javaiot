package com.factorylink.admin.customize.service.login.dto;

import com.factorylink.common.enums.dictionary.DictionaryData;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class ConfigDTO {

    private Map<String, List<DictionaryData>> dictionary;

}
