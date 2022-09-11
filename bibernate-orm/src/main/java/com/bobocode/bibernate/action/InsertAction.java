package com.bobocode.bibernate.action;

import com.bobocode.bibernate.session.impl.JDBCRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InsertAction implements Action {

    private final Object entity;
    private final JDBCRepository jdbcRepository;


    @Override
    public void perform() {
        jdbcRepository.save(entity);
    }
}
