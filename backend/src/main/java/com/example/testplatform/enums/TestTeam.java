package com.example.testplatform.enums;

/**
 * 测试团队（值域固定）
 */
public enum TestTeam {

    QIJIN("企金测试团队"),

    LINGSHOU("零售测试团队"),

    SHUJU("数据测试团队"),

    GONGGONG("公共测试团队");

    /** 值域正则，供参数校验注解使用（编译期常量） */
    public static final String REGEX = "企金测试团队|零售测试团队|数据测试团队|公共测试团队";

    /** 值域提示，供参数校验注解使用（编译期常量） */
    public static final String TIP = "企金测试团队/零售测试团队/数据测试团队/公共测试团队";

    private final String name;

    TestTeam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * 是否为合法的测试团队
     */
    public static boolean contains(String name) {
        if (name == null) {
            return false;
        }
        for (TestTeam team : values()) {
            if (team.name.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
