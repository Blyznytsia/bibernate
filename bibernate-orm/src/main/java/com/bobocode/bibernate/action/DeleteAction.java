package com.bobocode.bibernate.action;

import com.bobocode.bibernate.session.impl.JDBCRepository;

public class DeleteAction implements Action{

    private Object entity;

    public DeleteAction(Object entity, JDBCRepository jdbcRepository) {
        this.entity = entity;
    }

    @Override
    public void perform() {

    }
}
