package com.github.shawven.calf.oplog.register.election;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author xw
 * @date 2022/12/9
 */
public abstract class AbstractElection implements Election {

    private final Logger logger = LoggerFactory.getLogger(AbstractElection.class);

    /**
     * leader selector state
     */
    private final AtomicReference<State> state = new AtomicReference<>(State.STARTED);



    /**
     * keep racing for leadership when losing
     */
    private final boolean autoRequeue;

    /**
     * invoke when acquire leadership
     */
    private final TaskListener taskListener;

    public AbstractElection(boolean autoRequeue, TaskListener taskListener) {
        this.autoRequeue = autoRequeue;
        this.taskListener = taskListener;
    }

    /**
     * start racing for leadership.
     */
    @Override
    public void start() {
        logger.info("election start racing for leadership");

        try {
            doStart();
            logger.debug("election start");
        } catch (Exception e) {
            logger.error("election has some error.", e);
            doEnd();
        }
    }

    @Override
    public void close() {
        if(State.HOLD.equals(state.get())){
            taskListener.end();
            state.compareAndSet(State.HOLD, State.STARTED);
        }
        state.set(State.CLOSE);
        logger.info("election has been closed");
    }

    protected void doStart() throws Exception {
        lockForStart();

        logger.debug("election successfully get Lock");

        boolean ready = !state.get().equals(State.CLOSE);
        if (ready){
            if(!(state.compareAndSet(State.STARTED, State.HOLD))) {
                throw new IllegalStateException("Leadership cannot be acquired without initiating the process");
            }
            startedCallback();
            taskListener.start();
        }
    }

    protected void doEnd() {
        try {
            if (state.get().equals(State.HOLD)) {
                taskListener.end();
                state.set(State.STARTED);
            }
        } catch (Exception e) {
            state.set(State.STARTED);
        }

        logger.debug("election has successfully canceled the task.");

        if(autoRequeue) {
            try {
                // 延迟领导选举
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            start();
        }
    }

    protected abstract void startedCallback() throws Exception;

    protected abstract void lockForStart() throws Exception;

    private enum State {
        //持有领导权
        HOLD,
        //准备领导竞争
        STARTED,
        //关闭
        CLOSE
    }
}