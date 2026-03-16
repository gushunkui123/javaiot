package com.agileboot.domain.system.post;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.domain.common.cache.CacheCenter;
import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.system.post.command.UpdatePostCommand;
import com.agileboot.domain.system.post.db.SysPostEntity;
import com.agileboot.domain.system.post.db.SysPostService;
import com.agileboot.domain.system.post.model.PostModel;
import com.agileboot.domain.system.post.model.PostModelFactory;
import com.agileboot.infrastructure.cache.redis.RedisCacheTemplate;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PostApplicationServiceTest {

    private final PostModelFactory postModelFactory = mock(PostModelFactory.class);
    private final SysPostService postService = mock(SysPostService.class);
    private final PostApplicationService applicationService = new PostApplicationService(postModelFactory, postService);

    @SuppressWarnings("unchecked")
    private final RedisCacheTemplate<SysPostEntity> postCache = mock(RedisCacheTemplate.class);

    private RedisCacheTemplate<SysPostEntity> originalPostCache;

    @BeforeEach
    void setUp() {
        originalPostCache = CacheCenter.postCache;
        CacheCenter.postCache = postCache;
    }

    @AfterEach
    void tearDown() {
        CacheCenter.postCache = originalPostCache;
    }

    @Test
    void updatePostShouldInvalidatePostCache() {
        UpdatePostCommand command = new UpdatePostCommand();
        command.setPostId(6L);
        PostModel postModel = mock(PostModel.class);
        when(postModelFactory.loadById(6L)).thenReturn(postModel);
        when(postModel.getPostId()).thenReturn(6L);

        applicationService.updatePost(command);

        verify(postModel).loadFromUpdateCommand(command);
        verify(postModel).checkPostNameUnique();
        verify(postModel).checkPostCodeUnique();
        verify(postModel).updateById();
        verify(postCache).delete(6L);
    }

    @Test
    void deletePostShouldInvalidateDeletedPostCaches() {
        PostModel postModel1 = mock(PostModel.class);
        PostModel postModel2 = mock(PostModel.class);
        when(postModelFactory.loadById(2L)).thenReturn(postModel1);
        when(postModelFactory.loadById(3L)).thenReturn(postModel2);

        applicationService.deletePost(new BulkOperationCommand<>(List.of(2L, 3L)));

        verify(postModel1).checkCanBeDelete();
        verify(postModel2).checkCanBeDelete();
        ArgumentCaptor<Collection<Long>> deletedIdsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(postService).removeBatchByIds(deletedIdsCaptor.capture());
        Collection<Long> deletedIds = deletedIdsCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(2, deletedIds.size());
        org.junit.jupiter.api.Assertions.assertTrue(deletedIds.containsAll(List.of(2L, 3L)));
        verify(postCache).delete(2L);
        verify(postCache).delete(3L);
    }
}
