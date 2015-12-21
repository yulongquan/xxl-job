package com.dianping.job.controller;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.job.client.handler.HandlerRepository;
import com.dianping.job.core.model.ReturnT;
import com.dianping.job.core.util.DynamicSchedulerUtil;
import com.dianping.job.service.job.HttpJobBean;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
public class IndexController {

	
	@RequestMapping("")
	public String index(Model model) {
		List<Map<String, Object>> jobList = DynamicSchedulerUtil.getJobList();
		model.addAttribute("jobList", jobList);
		return "job/index";
	}
	
	@RequestMapping("/help")
	public String help(Model model) {
		return "job/help";
	}
	
	@RequestMapping("/job/add")
	@ResponseBody
	public ReturnT<String> add(HttpServletRequest request) {
		String triggerKeyName = null;
		String cronExpression = null;
		Map<String, Object> jobData = new HashMap<String, Object>();
		
		try {
			request.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		Set<Map.Entry<String, String[]>> paramSet = request.getParameterMap().entrySet();
		for (Entry<String, String[]> param : paramSet) {
			if (param.getKey().equals("triggerKeyName")) {
				triggerKeyName = param.getValue()[0];
			} else if (param.getKey().equals("cronExpression")) {
				cronExpression = param.getValue()[0];
			} else {
				jobData.put(param.getKey(), param.getValue().length>0?param.getValue()[0]:param.getValue());
			}
		}
		
		// triggerKeyName
		if (StringUtils.isBlank(triggerKeyName)) {
			return new ReturnT<String>(500, "请输入“任务key”");
		}
		
		// cronExpression
		if (StringUtils.isBlank(cronExpression)) {
			return new ReturnT<String>(500, "请输入“任务corn”");
		}
		if (!CronExpression.isValidExpression(cronExpression)) {
			return new ReturnT<String>(500, "“任务corn”不合法");
		}
		
		// jobData
		if (jobData.get(DynamicSchedulerUtil.job_desc)==null || jobData.get(DynamicSchedulerUtil.job_desc).toString().trim().length()==0) {
			return new ReturnT<String>(500, "请输入“任务描述”");
		}
		if (jobData.get(DynamicSchedulerUtil.job_url)==null || jobData.get(DynamicSchedulerUtil.job_url).toString().trim().length()==0) {
			return new ReturnT<String>(500, "请输入“任务URL”");
		}
		if (jobData.get(HandlerRepository.handleName)==null || jobData.get(HandlerRepository.handleName).toString().trim().length()==0) {
			return new ReturnT<String>(500, "请输入“任务handler”");
		}
		
		// jobClass
		Class<? extends Job> jobClass = HttpJobBean.class;
		
		try {
			boolean result = DynamicSchedulerUtil.addJob(triggerKeyName, cronExpression, jobClass, jobData);
			if (!result) {
				return new ReturnT<String>(500, "任务ID重复，请更换确认");
			}
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return ReturnT.FAIL;
	}
	
	@RequestMapping("/job/reschedule")
	@ResponseBody
	public ReturnT<String> reschedule(String triggerKeyName, String cronExpression) {
		// triggerKeyName
		if (StringUtils.isBlank(triggerKeyName)) {
			return new ReturnT<String>(500, "请输入“任务key”");
		}
		// cronExpression
		if (StringUtils.isBlank(cronExpression)) {
			return new ReturnT<String>(500, "请输入“任务corn”");
		}
		if (!CronExpression.isValidExpression(cronExpression)) {
			return new ReturnT<String>(500, "“任务corn”不合法");
		}
		try {
			DynamicSchedulerUtil.rescheduleJob(triggerKeyName, cronExpression);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return ReturnT.FAIL;
	}
	
	@RequestMapping("/job/remove")
	@ResponseBody
	public ReturnT<String> remove(String triggerKeyName) {
		try {
			DynamicSchedulerUtil.removeJob(triggerKeyName);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return ReturnT.FAIL;
		}
	}
	
	@RequestMapping("/job/pause")
	@ResponseBody
	public ReturnT<String> pause(String triggerKeyName) {
		try {
			DynamicSchedulerUtil.pauseJob(triggerKeyName);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return ReturnT.FAIL;
		}
	}
	
	@RequestMapping("/job/resume")
	@ResponseBody
	public ReturnT<String> resume(String triggerKeyName) {
		try {
			DynamicSchedulerUtil.resumeJob(triggerKeyName);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return ReturnT.FAIL;
		}
	}
	
	@RequestMapping("/job/trigger")
	@ResponseBody
	public ReturnT<String> triggerJob(String triggerKeyName) {
		try {
			DynamicSchedulerUtil.triggerJob(triggerKeyName);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return ReturnT.FAIL;
		}
	}
	
}
