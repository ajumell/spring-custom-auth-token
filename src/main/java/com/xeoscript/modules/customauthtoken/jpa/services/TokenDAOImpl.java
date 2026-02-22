package com.xeoscript.modules.customauthtoken.jpa.services;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.xeoscript.modules.customauthtoken.dao.TokenDAO;
import com.xeoscript.modules.customauthtoken.jpa.entity.QTokenEntity;
import com.xeoscript.modules.customauthtoken.jpa.entity.TokenEntity;
import com.xeoscript.modules.customauthtoken.model.enums.TokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class TokenDAOImpl implements TokenDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public void save(TokenEntity entity) {
        entityManager.persist(entity);
    }

    @Override
    public TokenEntity findByTokenValue(String tokenValue) {
        QTokenEntity q = QTokenEntity.tokenEntity;
        return queryFactory.selectFrom(q)
                .where(q.tokenValue.eq(tokenValue))
                .fetchOne();
    }

    @Override
    @Transactional
    public long atomicValidateAndIncrement(String tokenValue, String parameter,
                                           String tokenType, LocalDateTime now) {
        QTokenEntity q = QTokenEntity.tokenEntity;

        BooleanBuilder where = new BooleanBuilder();
        where.and(q.tokenValue.eq(tokenValue));
        where.and(q.parameterValue.eq(parameter));
        where.and(q.status.in(TokenStatus.ACTIVE, TokenStatus.PARTIALLY_USED));
        where.and(q.expiryTime.gt(now));
        where.and(q.usageLimit.isNull().or(q.usageCount.lt(q.usageLimit)));

        if (tokenType != null) {
            where.and(q.tokenType.eq(tokenType));
        }

        return queryFactory.update(q)
                .set(q.usageCount, q.usageCount.add(1))
                .set(q.usedAt, now)
                .set(q.updatedAt, now)
                .set(q.status, new CaseBuilder()
                        .when(q.usageLimit.isNotNull().and(q.usageCount.add(1).goe(q.usageLimit)))
                        .then(TokenStatus.USED)
                        .when(q.usageCount.add(1).gt(0))
                        .then(TokenStatus.PARTIALLY_USED)
                        .otherwise(q.status))
                .where(where)
                .execute();
    }

    @Override
    @Transactional
    public long invalidate(String tokenValue) {
        QTokenEntity q = QTokenEntity.tokenEntity;

        return queryFactory.update(q)
                .set(q.status, TokenStatus.INVALIDATED)
                .set(q.updatedAt, LocalDateTime.now())
                .where(q.tokenValue.eq(tokenValue)
                        .and(q.status.in(TokenStatus.ACTIVE, TokenStatus.PARTIALLY_USED)))
                .execute();
    }

    @Override
    @Transactional
    public long deleteExpired(LocalDateTime before) {
        QTokenEntity q = QTokenEntity.tokenEntity;

        return queryFactory.delete(q)
                .where(q.expiryTime.lt(before))
                .execute();
    }

    @Override
    public TokenEntity findByTokenValueAndParameter(String tokenValue, String parameter) {
        QTokenEntity q = QTokenEntity.tokenEntity;
        return queryFactory.selectFrom(q)
                .where(q.tokenValue.eq(tokenValue)
                        .and(q.parameterValue.eq(parameter)))
                .fetchOne();
    }
}
