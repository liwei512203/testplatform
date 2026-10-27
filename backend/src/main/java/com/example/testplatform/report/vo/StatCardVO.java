package com.example.testplatform.report.vo;

/**
 * 指标卡片 VO（含环比）
 */
public class StatCardVO {

    /** 指标键：projectCount/executed/covered/successRate/fail */
    private String key;

    /** 指标名称 */
    private String label;

    /** 当前值（数值型，比率已乘100保留两位小数） */
    private double value;

    /** 值展示后缀（如 %） */
    private String suffix;

    /** 上一周期值，无数据为 null */
    private Double prevValue;

    /**
     * 环比百分比 = (cur-prev)/prev*100，保留两位；
     * 无对照数据为 null
     */
    private Double changeRate;

    /** 环比方向语义：up-good / down-good（失败类指标上升是劣化） */
    private String direction;

    public StatCardVO() {
    }

    public StatCardVO(String key, String label, double value, String suffix,
                      Double prevValue, Double changeRate, String direction) {
        this.key = key;
        this.label = label;
        this.value = value;
        this.suffix = suffix;
        this.prevValue = prevValue;
        this.changeRate = changeRate;
        this.direction = direction;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Double getPrevValue() {
        return prevValue;
    }

    public void setPrevValue(Double prevValue) {
        this.prevValue = prevValue;
    }

    public Double getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(Double changeRate) {
        this.changeRate = changeRate;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
