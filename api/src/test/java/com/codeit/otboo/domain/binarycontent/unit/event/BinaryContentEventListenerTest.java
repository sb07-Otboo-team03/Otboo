package com.codeit.otboo.domain.binarycontent.unit.event;

import com.codeit.otboo.domain.binarycontent.event.BinaryContentDeletedEvent;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentEventListener;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentListDeletedEvent;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentRetryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class BinaryContentEventListenerTest {
    @Mock
    private BinaryContentRetryService binaryContentRetryService;

    @InjectMocks
    private BinaryContentEventListener binaryContentEventListener;

    @Nested
    @DisplayName("delete 이벤트 수신")
    class BinaryContentEventListenerDeleteEvent {
        @Test
        @DisplayName("성공: 삭제 이벤트가 수신되면 delete 가 호출된다")
        void success_delete_event(){
            // given
            UUID binaryContentId = UUID.randomUUID();
            BinaryContentDeletedEvent event = new BinaryContentDeletedEvent(binaryContentId);

            // when
            binaryContentEventListener.handleDeleted(event);

            // then
            then(binaryContentRetryService).should()
                    .delete(binaryContentId);
        }
    }

    @Nested
    @DisplayName("deleteAll 이벤트 수신")
    class BinaryContentEventListenerDeleteAllEvent {
        @Test
        @DisplayName("성공: 삭제 이벤트가 수신되면 deleteAll이 호출된다")
        void success_delete_event(){
            // given
            List<UUID> binaryContentIdList = List.of(UUID.randomUUID());
            BinaryContentListDeletedEvent event = new BinaryContentListDeletedEvent(binaryContentIdList);

            // when
            binaryContentEventListener.handleDeletedAll(event);

            // then
            then(binaryContentRetryService).should()
                    .deleteAll(binaryContentIdList);
        }
    }
}
