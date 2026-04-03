package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public static final String LOG_MESSAGE_FORMAT = "save manager :: 담당자 저장 요청 / userId: %s / todoId: %s / managerUserId: %s";

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Log saveLog(Long userId, Long todoId, Long managerUserId) {
        String message = String.format(LOG_MESSAGE_FORMAT, userId, todoId, managerUserId);
        return logRepository.save(Log.create(message));
    }
}
