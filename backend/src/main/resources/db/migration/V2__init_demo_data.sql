-- 演示数据，便于首次启动后直接查看效果
INSERT INTO `project` (`id`, `name`, `system_code`) VALUES
  ('p-demo-0001', '交易系统', 'TRADE'),
  ('p-demo-0002', '支付系统', 'PAY');

INSERT INTO `test_case` (`project_id`, `name`, `content`, `create_time`, `update_time`) VALUES
  ('p-demo-0001', '下单成功-余额充足', '{"precondition":"用户已登录且账户余额大于订单金额","steps":["进入商品详情页","点击立即购买","提交订单"],"expected":"订单创建成功，页面跳转到支付页"}', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
  ('p-demo-0001', '下单失败-余额不足', '{"precondition":"用户已登录且账户余额小于订单金额","steps":["进入商品详情页","点击立即购买","提交订单"],"expected":"提示余额不足，订单未创建"}', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
  ('p-demo-0002', '支付成功-银行卡支付', '{"precondition":"存在一笔待支付订单","steps":["进入收银台","选择银行卡支付","输入支付密码"],"expected":"支付成功，订单状态变为已支付"}', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
