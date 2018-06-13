package org.coredata.core.data;

import java.util.List;

import org.coredata.core.data.exception.DataException;

import com.google.common.collect.Lists;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;

public class WaitStrategyFactory {

    private static final List<String> WAIT_STRATEGY_SUPPORTED = Lists.newArrayList(BlockingWaitStrategy.class.getName(),
            BusySpinWaitStrategy.class.getName(), SleepingWaitStrategy.class.getName(), YieldingWaitStrategy.class.getName());

    /**
     * 构造线程等待策略
     */
    public static WaitStrategy build(String name) {
        if (WAIT_STRATEGY_SUPPORTED.contains(name)) {
            try {
                return (WaitStrategy) Class.forName(name).newInstance();
            } catch (InstantiationException e) {
                throw new DataException(e);
            } catch (IllegalAccessException e) {
                throw new DataException(e);
            } catch (ClassNotFoundException e) {
                throw new DataException(e);
            }
        } else {
            throw new DataException("Invalid wait strategy: " + name);
        }
    }
}
