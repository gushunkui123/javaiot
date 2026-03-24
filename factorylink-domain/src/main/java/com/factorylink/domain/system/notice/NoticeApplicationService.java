package com.factorylink.domain.system.notice;

import com.factorylink.common.core.page.PageDTO;
import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.factorylink.domain.system.notice.command.NoticeAddCommand;
import com.factorylink.domain.system.notice.command.NoticeUpdateCommand;
import com.factorylink.domain.system.notice.dto.NoticeDTO;
import com.factorylink.domain.system.notice.model.NoticeModel;
import com.factorylink.domain.system.notice.model.NoticeModelFactory;
import com.factorylink.domain.system.notice.query.NoticeQuery;
import com.factorylink.domain.system.notice.db.SysNoticeEntity;
import com.factorylink.domain.system.notice.db.SysNoticeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
public class NoticeApplicationService {

    private final SysNoticeService noticeService;

    private final NoticeModelFactory noticeModelFactory;

    private final AuditUserEnricher auditUserEnricher;

    public PageDTO<NoticeDTO> getNoticeList(NoticeQuery query) {
        Page<SysNoticeEntity> page = noticeService.getNoticeList(query.toPage(), query.toQueryWrapper());
        List<NoticeDTO> records = page.getRecords().stream().map(NoticeDTO::new).collect(Collectors.toList());
        auditUserEnricher.enrich(records);
        return new PageDTO<>(records, page.getTotal());
    }


    public NoticeDTO getNoticeInfo(Long id) {
        NoticeModel noticeModel = noticeModelFactory.loadById(id);
        NoticeDTO dto = new NoticeDTO(noticeModel);
        auditUserEnricher.enrich(dto);
        return dto;
    }


    public void addNotice(NoticeAddCommand addCommand) {
        NoticeModel noticeModel = noticeModelFactory.create();
        noticeModel.loadAddCommand(addCommand);

        noticeModel.checkFields();

        noticeModel.insert();
    }


    public void updateNotice(NoticeUpdateCommand updateCommand) {
        NoticeModel noticeModel = noticeModelFactory.loadById(updateCommand.getNoticeId());
        noticeModel.loadUpdateCommand(updateCommand);

        noticeModel.checkFields();

        noticeModel.updateById();
    }

    public void deleteNotice(BulkOperationCommand<Integer> deleteCommand) {
        noticeService.removeBatchByIds(deleteCommand.getIds());
    }




}
