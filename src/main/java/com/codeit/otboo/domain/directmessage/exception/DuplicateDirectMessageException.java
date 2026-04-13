//package com.codeit.otboo.domain.directmessage.exception;
//
//import com.codeit.otboo.global.exception.ErrorCode;
//import java.util.Map;
//import java.util.UUID;
//import org.springframework.http.HttpStatus;
//
//public class DuplicateDirectMessageException extends DirectMessageException{
//
//    public DuplicateDirectMessageException() {
//        super(ErrorCode.DUPLICATE_DIRECTMESSAGE,
//            HttpStatus.CONFLICT);
//    }
//
//    public DuplicateDirectMessageException(UUID senderId, UUID receiverId) {
//        super(ErrorCode.DUPLICATE_DIRECTMESSAGE,
//            Map.of("senderId : ", senderId.toString(),
//                "receiverId : ", receiverId.toString()),
//            HttpStatus.CONFLICT);
//    }
//}
