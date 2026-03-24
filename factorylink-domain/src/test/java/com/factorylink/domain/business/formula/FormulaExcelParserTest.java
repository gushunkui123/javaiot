package com.factorylink.domain.business.formula;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.factorylink.domain.business.formula.dto.FormulaExcelDTO;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class FormulaExcelParserTest {

    @Test
    void parseShouldExtractHeaderFields() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("配方.xlsx");
        assertNotNull(is, "测试Excel文件应该存在");

        FormulaExcelDTO dto = FormulaExcelParser.parse(is);

        assertNotNull(dto);
        assertNotNull(dto.getFormulaCode(), "配方编号不应为空");
        assertNotNull(dto.getItems(), "配方明细列表不应为空");
        assertFalse(dto.getItems().isEmpty(), "配方明细不应为空");
    }

    @Test
    void parseShouldExtractItemDetails() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("配方.xlsx");
        assertNotNull(is);

        FormulaExcelDTO dto = FormulaExcelParser.parse(is);

        for (FormulaExcelDTO.Item item : dto.getItems()) {
            assertNotNull(item.getCode(), "原料代号不应为空");
            assertTrue(item.getSortOrder() > 0, "排序号应为正数");
        }
    }

    @Test
    void parseShouldAssignSequentialSortOrder() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("配方.xlsx");
        assertNotNull(is);

        FormulaExcelDTO dto = FormulaExcelParser.parse(is);

        for (int i = 0; i < dto.getItems().size(); i++) {
            assertTrue(dto.getItems().get(i).getSortOrder() == i + 1,
                "排序号应从1开始递增");
        }
    }

    @Test
    void parseShouldFailOnInvalidStream() {
        InputStream badStream = new ByteArrayInputStream(new byte[]{0, 1, 2, 3});
        assertThrows(Exception.class, () -> FormulaExcelParser.parse(badStream));
    }
}
