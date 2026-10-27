package com.example.testplatform.report.vo;

import java.util.List;

/**
 * 系统级/项目级总览报表 VO
 */
public class OverviewVO {

    /** 指标卡片（含环比） */
    private List<StatCardVO> cards;

    /** 趋势点列（当前周期） */
    private List<TrendPointVO> trend;

    /** 趋势点列（上一对照周期，长度与 trend 对齐使用） */
    private List<TrendPointVO> prevTrend;

    /** 趋势粒度：day / week */
    private String granularity;

    /** 按执行方式统计（成功率分方式展示） */
    private List<TypeStatVO> typeStats;

    /** 数据截至日期（T+1 跑批口径） */
    private String dataThroughDate;

    public List<StatCardVO> getCards() {
        return cards;
    }

    public void setCards(List<StatCardVO> cards) {
        this.cards = cards;
    }

    public List<TrendPointVO> getTrend() {
        return trend;
    }

    public void setTrend(List<TrendPointVO> trend) {
        this.trend = trend;
    }

    public List<TrendPointVO> getPrevTrend() {
        return prevTrend;
    }

    public void setPrevTrend(List<TrendPointVO> prevTrend) {
        this.prevTrend = prevTrend;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public List<TypeStatVO> getTypeStats() {
        return typeStats;
    }

    public void setTypeStats(List<TypeStatVO> typeStats) {
        this.typeStats = typeStats;
    }

    public String getDataThroughDate() {
        return dataThroughDate;
    }

    public void setDataThroughDate(String dataThroughDate) {
        this.dataThroughDate = dataThroughDate;
    }
}
