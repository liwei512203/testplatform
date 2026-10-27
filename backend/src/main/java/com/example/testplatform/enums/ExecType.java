package com.example.testplatform.enums;

/**
 * 执行方式（值域固定）
 */
public enum ExecType {

    MANUAL("手动执行"),

    TIMER("定时任务执行"),

    PIPELINE("流水线触发执行");

    /** 值域正则，供参数校验注解使用（编译期常量） */
    public static final String REGEX = "手动执行|定时任务执行|流水线触发执行";

    /** 值域提示 */
    public static final String TIP = "手动执行/定时任务执行/流水线触发执行";

    private final String name;

    ExecType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * 是否为合法的执行方式
     */
    public static boolean contains(String name) {
        if (name == null) {
            return false;
        }
        for (ExecType type : values()) {
            if (type.name.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
