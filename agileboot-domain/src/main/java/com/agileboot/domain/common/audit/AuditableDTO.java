package com.agileboot.domain.common.audit;

import java.util.Date;

/**
 * 审计字段 DTO 统一约定
 *
 * @author Codex
 */
public interface AuditableDTO {

    Long getCreatorId();

    void setCreatorId(Long creatorId);

    String getCreatorName();

    void setCreatorName(String creatorName);

    Date getCreateTime();

    void setCreateTime(Date createTime);

    Long getUpdaterId();

    void setUpdaterId(Long updaterId);

    String getUpdaterName();

    void setUpdaterName(String updaterName);

    Date getUpdateTime();

    void setUpdateTime(Date updateTime);

}
