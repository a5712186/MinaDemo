package com.tjc.mina_demo;

import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.util.logging.Logger;

public class HeartBeatListener implements IoServiceListener{
    private final static Logger log = Logger.getLogger(HeartBeatListener.class.getSimpleName());

    private NioSocketConnector connector;

    public HeartBeatListener(NioSocketConnector connector) {
        this.connector = connector;
    }

    @Override
    public void serviceActivated(IoService service) throws Exception {
        log.info("当service生效时被调用");
    }

    @Override
    public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {
        log.info("当service空闲时被调用,无效不会调用");
    }

    @Override
    public void serviceDeactivated(IoService service) throws Exception {
        log.info(" 当service失效时被调用");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        log.info("当新的session生成时被调用");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        log.info("当session被注销时调用");
    }

    @Override
    public void sessionDestroyed(IoSession session) throws Exception {
        log.info("当session被销毁时调用");
    }
}
