package com.hengyu.chapter39.good.service;

import java.util.Date;
import java.util.UUID;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hengyu.chapter39.good.entity.GoodInfoEntity;
import com.hengyu.chapter39.timers.GoodAddTimer;
import com.hengyu.chapter39.timers.GoodSecKillRemindTimer;
import com.hengyu.chapter39.timers.GoodStockCheckTimer;


/**
 * 商品业务逻辑
 * ========================
 *
 * @author 恒宇少年
 * Created with IntelliJ IDEA.
 * Date：2017/11/5
 * Time：15:04
 * 码云：http://git.oschina.net/jnyqy
 * ========================
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class GoodInfoService {
    /**
     * 注入任务调度器
     */
    @Autowired
    private Scheduler scheduler;
    /**
     * 保存商品基本信息
     *
     * @param good 商品实例
     * @return
     */
    public Long saveGood(GoodInfoEntity good) throws Exception {
    	System.out.println("添加时间："+System.currentTimeMillis());
        //构建创建商品定时任务
        buildCreateGoodTimer();
        //构建商品库存定时任务
        //buildGoodStockTimer();
        //构建商品描述提醒定时任务
        buildGoodSecKillRemindTimer(good.getId());
        return good.getId();
    }

    /**
     * 构建创建商品定时任务
     */
    public void buildCreateGoodTimer() throws Exception {
        //设置开始时间为1分钟后
        long startAtTime = System.currentTimeMillis() + 1000 * 10;
        //任务名称
        String name = UUID.randomUUID().toString();
        //任务所属分组
        String group = GoodAddTimer.class.getName();
        //创建任务
        JobDetail jobDetail = JobBuilder.newJob(GoodAddTimer.class)
        		//故障可恢复job，job失败时，它将重新运行
        		.requestRecovery(true)
        		.withIdentity(name, group).build();
        //创建任务触发器
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group).startAt(new Date(startAtTime))
        		.withSchedule(SimpleScheduleBuilder.simpleSchedule()
        				//设置失败指令:表示当job因为job执行时间过长 而 错过触发器时 job执行完后立即再次执行job
        				.withMisfireHandlingInstructionFireNow())
        		.build();
        //将触发器与任务绑定到调度器内
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 构建商品库存定时任务
     *
     * @throws Exception
     */
    public void buildGoodStockTimer() throws Exception {
        //任务名称
        String name = UUID.randomUUID().toString();
        //任务所属分组
        String group = GoodStockCheckTimer.class.getName();

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0/30 * * * * ?");
        //创建任务
        JobDetail jobDetail = JobBuilder.newJob(GoodStockCheckTimer.class).withIdentity(name, group).build();
        //创建任务触发器
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group).withSchedule(scheduleBuilder).build();
        //将触发器与任务绑定到调度器内
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 构建商品秒杀提醒定时任务
     * 设置五分钟后执行
     *
     * @throws Exception
     */
    public void buildGoodSecKillRemindTimer(Long goodId) throws Exception {
        //任务名称
        String name = UUID.randomUUID().toString();
        //任务所属分组
        String group = GoodSecKillRemindTimer.class.getName();
        //秒杀开始时间
        long startTime = System.currentTimeMillis() + 1000 * 5 * 10;
        JobDetail jobDetail = JobBuilder
                .newJob(GoodSecKillRemindTimer.class)
                .withIdentity(name, group)
                .build();

        //设置任务传递商品编号参数
        jobDetail.getJobDataMap().put("goodId", goodId);

        //创建任务触发器
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group).startAt(new Date(startTime)).build();
        //将触发器与任务绑定到调度器内
        scheduler.scheduleJob(jobDetail, trigger);
    }
    
    /**
     * 获取Job状态
     * @param jobName
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    public String getJobState(String jobName, String jobGroup) throws SchedulerException {             
        TriggerKey triggerKey = new TriggerKey(jobName, jobGroup);    
        return scheduler.getTriggerState(triggerKey).name();
      }
    
    //暂停所有任务
    public void pauseAllJob() throws SchedulerException {            
        scheduler.pauseAll();
     }
   
   //暂停任务
   public String pauseJob(String jobName, String jobGroup) throws SchedulerException {            
       JobKey jobKey = new JobKey(jobName, jobGroup);
       JobDetail jobDetail = scheduler.getJobDetail(jobKey);
       if (jobDetail == null) {
            return "fail";
       }else {
            scheduler.pauseJob(jobKey);
            return "success";
       }
                                    
   }
   
   //恢复所有任务
   public void resumeAllJob() throws SchedulerException {            
       scheduler.resumeAll();
   }
   
   // 恢复某个任务
   public String resumeJob(String jobName, String jobGroup) throws SchedulerException {
       
       JobKey jobKey = new JobKey(jobName, jobGroup);
       JobDetail jobDetail = scheduler.getJobDetail(jobKey);
       if (jobDetail == null) {
           return "fail";
       }else {               
           scheduler.resumeJob(jobKey);
           return "success";
       }
   }
   
   //删除某个任务
   public String  deleteJob(String jobName, String jobGroup) throws SchedulerException {            
       JobKey jobKey = new JobKey(jobName, jobGroup);
       JobDetail jobDetail = scheduler.getJobDetail(jobKey);
       if (jobDetail == null ) {
            return "jobDetail is null";
       }else if(!scheduler.checkExists(jobKey)) {
           return "jobKey is not exists";
       }else {
            scheduler.deleteJob(jobKey);
            return "success";
       }  
      
   }
   
	/*
	 * //修改任务 public String modifyJob(AppQuartz appQuartz) throws SchedulerException
	 * { if (!CronExpression.isValidExpression(appQuartz.getCronExpression())) {
	 * return "Illegal cron expression"; } TriggerKey triggerKey =
	 * TriggerKey.triggerKey(appQuartz.getJobName(),appQuartz.getJobGroup()); JobKey
	 * jobKey = new JobKey(appQuartz.getJobName(),appQuartz.getJobGroup()); if
	 * (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
	 * CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
	 * //表达式调度构建器,不立即执行 CronScheduleBuilder scheduleBuilder =
	 * CronScheduleBuilder.cronSchedule(appQuartz.getCronExpression()).
	 * withMisfireHandlingInstructionDoNothing(); //按新的cronExpression表达式重新构建trigger
	 * trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
	 * .withSchedule(scheduleBuilder).build(); //修改参数
	 * if(!trigger.getJobDataMap().get("invokeParam").equals(appQuartz.
	 * getInvokeParam())) {
	 * trigger.getJobDataMap().put("invokeParam",appQuartz.getInvokeParam()); }
	 * //按新的trigger重新设置job执行 scheduler.rescheduleJob(triggerKey, trigger); return
	 * "success"; }else { return "job or trigger not exists"; }
	 * 
	 * }
	 */
}
