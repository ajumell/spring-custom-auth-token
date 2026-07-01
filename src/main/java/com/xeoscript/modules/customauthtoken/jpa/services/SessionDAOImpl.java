package com.xeoscript.modules.customauthtoken.jpa.services;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.xeoscript.modules.customauthtoken.dao.SessionDAO;
import com.xeoscript.modules.customauthtoken.jpa.entity.QTokenEntity;
import com.xeoscript.modules.customauthtoken.jpa.entity.QTokenSessionEntity;
import com.xeoscript.modules.customauthtoken.jpa.entity.TokenSessionEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SessionDAOImpl implements SessionDAO {

    private final EntityManager entityManager;

    private final JPAQueryFactory queryFactory;

    public SessionDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    @Transactional
    public void save(TokenSessionEntity entity) {
        entityManager.persist(entity);
    }

    @Override
    public TokenSessionEntity findByTokenValueAndKey(String tokenValue, String sessionKey) {
        QTokenSessionEntity q = QTokenSessionEntity.tokenSessionEntity;
        return queryFactory.selectFrom(q)
                .where(q.tokenValue.eq(tokenValue)
                        .and(q.sessionKey.eq(sessionKey)))
                .fetchOne();
    }

    @Override
    public List<TokenSessionEntity> findByTokenValue(String tokenValue) {
        QTokenSessionEntity q = QTokenSessionEntity.tokenSessionEntity;
        return queryFactory.selectFrom(q)
                .where(q.tokenValue.eq(tokenValue))
                .fetch();
    }

    @Override
    @Transactional
    public long deleteByTokenValueAndKey(String tokenValue, String sessionKey) {
        QTokenSessionEntity q = QTokenSessionEntity.tokenSessionEntity;
        return queryFactory.delete(q)
                .where(q.tokenValue.eq(tokenValue)
                        .and(q.sessionKey.eq(sessionKey)))
                .execute();
    }

    @Override
    @Transactional
    public long deleteByTokenValue(String tokenValue) {
        QTokenSessionEntity q = QTokenSessionEntity.tokenSessionEntity;
        return queryFactory.delete(q)
                .where(q.tokenValue.eq(tokenValue))
                .execute();
    }

    @Override
    @Transactional
    public long deleteExpiredByTokens(LocalDateTime before) {
        QTokenSessionEntity qs = QTokenSessionEntity.tokenSessionEntity;
        QTokenEntity qt = QTokenEntity.tokenEntity;

        JPAQuery<String> where = queryFactory
                .select(qt.tokenValue)
                .from(qt)
                .where(qt.expiryTime.lt(before));

        BooleanExpression query = qs.tokenValue.in(where);
        return queryFactory.delete(qs)
                .where(query)
                .execute();
    }
}

