package com.hengyu.chapter39;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.hengyu.chapter39.good.service.GoodInfoService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Chapter47ApplicationTests {

	@Autowired
    private GoodInfoService goodInfoService;
	
	@Test
	public void contextLoads() throws SchedulerException {
		goodInfoService.deleteJob("fc31bcbb-ddca-441d-a41c-99db391c05a0", "com.hengyu.chapter39.timers.GoodStockCheckTimer");
	}

}
