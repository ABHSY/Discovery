package com.nepxion.discovery.plugin.framework.decorator;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.nepxion.discovery.common.entity.WeightFilterEntity;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.framework.loadbalance.WeightRandomLoadBalance;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

//核心类  重写ribbon集成Eureka的负载均衡策略
public class ZoneAvoidanceRuleDecorator extends ZoneAvoidanceRule {
    @Autowired
    private PluginAdapter pluginAdapter;

    private WeightRandomLoadBalance weightRandomLoadBalance;

    @PostConstruct
    private void initialize() {
        weightRandomLoadBalance = new WeightRandomLoadBalance();
        weightRandomLoadBalance.setPluginAdapter(pluginAdapter);
    }

    @Override
    public Server choose(Object key) {
        //拿到实现配置好的权重   想看具体在choose方法中
        WeightFilterEntity weightFilterEntity = weightRandomLoadBalance.getWeightFilterEntity();
        if (weightFilterEntity == null) {
            return super.choose(key);
        }

        if (!weightFilterEntity.hasWeight()) {
            return super.choose(key);
        }
        //拿到所有的Server 进行过滤
        List<Server> eligibleServers = getPredicate().getEligibleServers(getLoadBalancer().getAllServers(), key);

        try {
            //返回符合的server
            return weightRandomLoadBalance.choose(eligibleServers, weightFilterEntity);
        } catch (Exception e) {
            return super.choose(key);
        }
    }
}