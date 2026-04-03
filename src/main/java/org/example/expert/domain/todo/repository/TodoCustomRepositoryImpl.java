package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {

        Todo result = jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> search(
            String keyword, String managerNickname, LocalDateTime startAt, LocalDateTime endAt, Pageable pageable
    ) {
        // 검색 결과 화면에 필요한 값만 Projection으로 조회
        List<TodoSearchResponse> content = jpaQueryFactory
                .select(
                        Projections.constructor(
                                TodoSearchResponse.class,
                                todo.id,
                                todo.title,
                                managerCountExpression(),
                                commentCountExpression()
                        )
                )
                .from(todo)
                .where(
                        titleContains(keyword),
                        createdAtBetween(startAt, endAt),
                        managerNicknameExists(managerNickname)
                )
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이지 정보를 계산 전체 일정 수 조회
        Long totalCount = jpaQueryFactory
                .select(todo.count())
                .from(todo)
                .where(
                        titleContains(keyword),
                        createdAtBetween(startAt, endAt),
                        managerNicknameExists(managerNickname)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }

    // 해당 일정에 연결된 전체 담당자 수를 계산
    private Expression<Long> managerCountExpression() {
        return JPAExpressions
                .select(manager.id.countDistinct())
                .from(manager)
                .where(manager.todo.id.eq(todo.id));
    }

    // 해당 일정에 연결된 전체 댓글 수를 계산
    private Expression<Long> commentCountExpression() {
        return JPAExpressions
                .select(comment.id.countDistinct())
                .from(comment)
                .where(comment.todo.id.eq(todo.id));
    }

    // 시작일과 종료일이 모두 있으면 범위 검색을 하고 하나만 있으면 포함해서 검색
    private BooleanExpression createdAtBetween(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null) {
            return todo.createdAt.between(startAt, endAt);
        } else if (startAt != null) {
            return todo.createdAt.goe(startAt);
        } else if (endAt != null) {
            return todo.createdAt.loe(endAt);
        } else {
            return null;
        }
    }

    // 부분 일치 검색 가능하게 containsIgnoreCase를 사용
    private BooleanExpression titleContains(String keyword) {
        return keyword != null && !keyword.isBlank()
                ? todo.title.containsIgnoreCase(keyword)
                : null;
    }

    // 일정 포함 여부 판단
    private BooleanExpression managerNicknameExists(String managerNickname) {
        if (managerNickname == null || managerNickname.isBlank()) {
            return null;
        }

        return JPAExpressions
                .selectOne()
                .from(manager)
                .join(manager.user, user)
                .where(
                        manager.todo.id.eq(todo.id),
                        user.nickname.containsIgnoreCase(managerNickname)
                )
                .exists();
    }
}
