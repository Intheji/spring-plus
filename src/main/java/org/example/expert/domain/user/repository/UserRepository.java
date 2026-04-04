package org.example.expert.domain.user.repository;

import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Page<User> findAllByNickname(String nickname, Pageable pageable);

    Slice<User> findByNickname(String nickname, Pageable pageable);

    @Query(value = """
    select new org.example.expert.domain.user.dto.response.UserResponse(u.id, u.email)
    from User u
    where u.nickname = :nickname
    """,
          countQuery = """
    select count(u)
    from User u
    where u.nickname = :nickname
    """)
    Page<UserResponse> findUserResponsesByNickname(@Param("nickname") String nickname, Pageable pageable);
}
