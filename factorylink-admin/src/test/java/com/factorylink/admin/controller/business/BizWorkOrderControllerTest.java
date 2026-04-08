package com.factorylink.admin.controller.business;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.factorylink.common.utils.jackson.JacksonUtil;
import com.factorylink.domain.business.workorder.WorkOrderApplicationService;
import com.factorylink.infrastructure.exception.GlobalExceptionInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class BizWorkOrderControllerTest {

    private final WorkOrderApplicationService workOrderApplicationService =
        org.mockito.Mockito.mock(WorkOrderApplicationService.class);


    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
            .standaloneSetup(new BizWorkOrderController(workOrderApplicationService))
            .setControllerAdvice(new GlobalExceptionInterceptor())
            .setValidator(validator)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(JacksonUtil.initMapper()))
            .build();
    }

    @Test
    void addShouldRejectInvalidOrderBatchNum() throws Exception {
        String requestBody = """
            {
              "workOrderNo": "WO-TEST-001",
              "orderDate": "2026-03-23 00:00:00",
              "lineNo": "A",
              "formulaCode": "FORMULA_001",
              "moldCode": "MOLD_001",
              "modelColor": "BLACK",
              "orderBatchNum": -1,
              "remark": "validation test"
            }
            """;

        mockMvc.perform(post("/business/workOrder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(103))
            .andExpect(jsonPath("$.msg").value("请求参数异常，计划批次数必须为正数"));

        verify(workOrderApplicationService, never()).addWorkOrder(any());
    }

    @Test
    void editShouldRejectMissingWorkOrderId() throws Exception {
        String requestBody = """
            {
              "workOrderNo": "WO-TEST-001",
              "orderDate": "2026-03-23 00:00:00",
              "lineNo": "A",
              "formulaCode": "FORMULA_001",
              "moldCode": "MOLD_001",
              "modelColor": "BLACK",
              "orderBatchNum": 3,
              "remark": "validation test"
            }
            """;

        mockMvc.perform(put("/business/workOrder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(103))
            .andExpect(jsonPath("$.msg").value("请求参数异常，工单ID不能为空"));

        verify(workOrderApplicationService, never()).updateWorkOrder(any());
    }

    @Test
    void removeShouldRejectEmptyIds() throws Exception {
        mockMvc.perform(delete("/business/workOrder")
                .param("ids", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(10003))
            .andExpect(jsonPath("$.msg").value("批量参数ID列表为空"));

        verify(workOrderApplicationService, never()).deleteWorkOrder(any());
    }
}
