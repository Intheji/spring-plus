package org.example.expert.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    public Page<UserResponse> getUsers(String nickname, Pageable pageable) {
        int pageNumber = pageable.getPageNumber();
        int requestedPage = pageNumber > 0 ? pageNumber : 1;
        PageRequest pageRequest = PageRequest.of(pageNumber > 0 ? pageNumber - 1 : 0, pageable.getPageSize());
        long startedAt = System.nanoTime();

        Page<User> users;
        if (ObjectUtils.isEmpty(nickname)) {
            users = userRepository.findAll(pageRequest);
        } else {
            users = userRepository.findAllByNickname(nickname, pageRequest);
        }

        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
        log.info(
                "유저 검색 완료 - 닉네임: {}, 페이지: {}, 요청 개수: {}, 반환 개수: {}, 전체 개수: {}, 소요 시간(ms): {}",
                ObjectUtils.isEmpty(nickname) ? "<전체 조회>" : nickname,
                requestedPage,
                pageRequest.getPageSize(),
                users.getNumberOfElements(),
                users.getTotalElements(),
                elapsedMillis
        );

        return users.map(user -> new UserResponse(user.getId(), user.getEmail()));
    }

    public Slice<UserResponse> getUsersWithSlice(String nickname, Pageable pageable) {
        int pageNumber = pageable.getPageNumber();
        int requestedPage = pageNumber > 0 ? pageNumber : 1;
        PageRequest pageRequest = PageRequest.of(pageNumber > 0 ? pageNumber - 1 : 0, pageable.getPageSize());

        long startedAt = System.nanoTime();

        Slice<User> users = userRepository.findByNickname(nickname, pageRequest);

        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
        log.info(
                "Slice 방식 유저 검색 완료 - 닉네임: {}, 페이지: {}, 요청 개수: {}, 반환 개수: {}, 다음 페이지 여부: {}, 소요 시간(ms): {}",
                nickname,
                requestedPage,
                pageRequest.getPageSize(),
                users.getNumberOfElements(),
                users.hasNext(),
                elapsedMillis
        );

        return users.map(user -> new UserResponse(user.getId(), user.getEmail()));
    }

    public Page<UserResponse> getUsersWithProjection(String nickname, Pageable pageable) {
        int pageNumber = pageable.getPageNumber();
        int requestedPage = pageNumber > 0 ? pageNumber : 1;
        PageRequest pageRequest = PageRequest.of(pageNumber > 0 ? pageNumber - 1 : 0, pageable.getPageSize());

        long startedAt = System.nanoTime();

        Page<UserResponse> users = userRepository.findUserResponsesByNickname(nickname, pageRequest);

        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
        log.info(
                "Projection 방식 유저 검색 완료 - 닉네임: {}, 페이지: {}, 요청 개수: {}, 반환 개수: {}, 전체 개수: {}, 소요 시간(ms): {}",
                nickname,
                requestedPage,
                pageRequest.getPageSize(),
                users.getNumberOfElements(),
                users.getTotalElements(),
                elapsedMillis
        );

        return users;
    }
}
