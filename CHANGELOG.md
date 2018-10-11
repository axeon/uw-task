[TOC]
# uw-task V2.6.1 版本升级
1. 修改队列任务日志记录模式,当发生错误时,总会记录全部日志

# uw-task V2.6.0 版本升级
1. 升级 spring-boot 至 2.0.5.RELEASE
2. 解决sendToQueue重试数据覆盖问题
3. TaskData添加retryType类型, 防止以runTask运行却发生了重试

# uw-task V2.1.2版本升级
1.增加数据错误类型TaskDataException，如果出现数据错误，则抛出此类异常。
2.去除了报文压缩，降低cpu占用率。升级后务必清除队列，并保证版本一致，否则可能会无法运行。
3.修正在管理界面上很难关闭队列的问题。

# uw-task V2.1.0版本升级
1.TaskData的runType默认为RUN_TYPE_AUTO_RPC模式，可以自动决断使用本地模式运行还是远程运行。
2.删除TaskScheduler的runTaskLocal和runTaskLocalSync，避免错误调用。
3.增加TaskScheduler的runTaskSync，增加同步调用方法。
4.uw.task.enable-task-registry现在默认为false，大家注意配置，否则可能会导致任务不启动。
5.uw.task.croner-thread-num并发线程默认为30，建议大家根据实际运行的croner数量设置。在enable-task-registry=false的时候，程序会强行把croner-thread-num设置为1，以节省资源。
6.TaskException更名为TaskPartnerException，并移动到uw.task.exception包下，以明确用途。