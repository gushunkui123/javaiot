package com.agileboot.domain.system.notice.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agileboot.domain.common.audit.AuditableDTO;
import com.agileboot.domain.system.notice.db.SysNoticeEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author valarchie
 */
@Data
@NoArgsConstructor
public class NoticeDTO implements AuditableDTO {

    public NoticeDTO(SysNoticeEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
            this.noticeId = entity.getNoticeId() + "";
        }
    }

    private String noticeId;

    private String noticeTitle;

    private Integer noticeType;

    private String noticeContent;

    private Integer status;

    private Long creatorId;

    private String creatorName;

    private Date createTime;

    private Long updaterId;

    private String updaterName;

    private Date updateTime;

}
