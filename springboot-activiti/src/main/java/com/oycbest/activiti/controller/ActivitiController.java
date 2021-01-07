package com.oycbest.activiti.controller;

import com.oycbest.activiti.service.impl.ActivityServiceImpl;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.*;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName ActivitiController
 * @Description TODO
 * @Author oyc
 * @Date 2020/12/13 23:19
 * @Version
 */
@RestController
@RequestMapping("act")
public class ActivitiController {

    @Autowired
    private IdentityService identityService;

    @RequestMapping("list")
    public String getProcessList() {
        User ouyang = identityService.newUser("ouyang");
        identityService.saveUser(ouyang);
        //List<String> userInfoKeys = identityService.getUserInfoKeys("ouyang");
        return "getProcessList:";
    }

    /**
     * 1.启动流程
     * 2.根据用户名获取待办流程 列表  （传入用户名）
     * 3.根据待办task id完成流程    （传入任务id）
     */
    public void startProccessInstance() {
        ProcessDefinition processDefinition = new ProcessDefinitionEntityImpl();
    }

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private ActivityServiceImpl activityService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    /**
     * 部署流程
     */
    @RequestMapping("initProcesses")
    public Deployment initProcesses() {
        //部署对象
        Deployment deployment = repositoryService.createDeployment()
                // bpmn文件
                .addClasspathResource("processes/holiday.bpmn")
                // 图片文件
                .addClasspathResource("processes/holiday.png")
                .name("请假申请流程")
                .deploy();
        System.out.println("流程部署id:" + deployment.getId());
        System.out.println("流程部署名称:" + deployment.getName());
        return deployment;
    }

    /**
     * 启动流程
     */
    @RequestMapping("startPro")
    public ResponseEntity startPro() {
        ProcessInstance processInstance = activityService.startProcesses();
        Map map = new HashMap();
        map.put("id",processInstance.getDeploymentId());
        map.put("name",processInstance.getName());
        map.put("processDefinitionId",processInstance.getProcessDefinitionId());
        map.put("startUserId",processInstance.getStartUserId());
        map.put("processDefinitionName",processInstance.getProcessDefinitionName());
        return ResponseEntity.ok(map);
    }

    /**
     * 获取待办任务列表
     */
    @RequestMapping("findTaskListByUserName")
    public ResponseEntity findTaskListByUserName(@Param("userName") String userName) {
        List<Task> taskList = activityService.findTaskByUserId(userName);
        //任务列表：[Task[id=5005, name=填写请假申请单]]
        System.out.println("任务列表：" + taskList);
        List resultList = new ArrayList();
        taskList.forEach(task -> {
            Map map = new HashMap();
            map.put("id",task.getId());
            map.put("name",task.getName());
            map.put("assignee",task.getAssignee());
            map.put("delegationState",task.getDelegationState());
            map.put("createTime",task.getCreateTime());
            map.put("processDefinitionId",task.getProcessDefinitionId());
            map.put("processInstanceId",task.getProcessInstanceId());
            resultList.add(map);
        });
        return ResponseEntity.ok(resultList);
    }

    /**
     * 完成任务
     */
    @RequestMapping("completeTaskById")
    public String completeTaskById(@RequestParam String taskId) {
        //完成任务后，任务列表数据减少
        activityService.completeTaskById(taskId);
        return "任务完成";
    }


    //设置流程变量
    //设置流程变量【基本类型】
    @RequestMapping("setTaskVar")
    public void setTaskVar() {
        List<Task> lists = activityService.findTaskByUserId("sly1");
        for (Task task : lists) {//不知为何，变量保存成功，但数据表只有请假天数含有任务id，单获取流程变量时，根据任务id均可获取到（如下一测试）
            taskService.setVariable(task.getId(), "请假人", "sly1");
            taskService.setVariableLocal(task.getId(), "请假天数", 3);
            taskService.setVariable(task.getId(), "请假日期", new Date());
        }
    }

    //获取流程变量
    @RequestMapping("getTaskVar")
    public void getTaskVar() {
        List<Task> lists = activityService.findTaskByUserId("sly1");
        for (Task task : lists) {
            //获取流程变量【基本类型】
            String person = (String) taskService.getVariable(task.getId(), "请假人");
            Integer day = (Integer) taskService.getVariableLocal(task.getId(), "请假天数");
            Date date = (Date) taskService.getVariable(task.getId(), "请假日期");

            System.out.println("流程变量：" + person + "||" + day + "||" + date + "||");
        }
    }

    //设置流程变量【实体】
    @RequestMapping("setTaskVarEntity")
    public void setTaskVarEntity() {
        List<Task> lists = activityService.findTaskByUserId("sly1");
        for (Task task : lists) {
           /* Person p = new Person();
            p.setName("翠花");
            p.setId(20);
            p.setDate();;
            p.setNote("回去探亲，一起吃个饭123");*/
            taskService.setVariable(task.getId(), "人员信息(添加固定版本)", "oy");

            System.out.println("设置流程变量成功！");
        }
    }

    //获取流程变量【实体】  实体必须序列化
    @RequestMapping("getTaskVarEntity")
    public void getTaskVarEntity() {
        List<Task> lists = activityService.findTaskByUserId("sly1");
        for (Task task : lists) {
            // 2.获取流程变量，使用javaBean类型
            Object variable = taskService.getVariable(task.getId(), "人员信息(添加固定版本)");
            System.out.println(variable);
            //System.out.println(" 请假人：  "+p.getName()+"  请假天数：  "+p.getId()+"   请假时间："+ p.getDate()+ "   请假原因： "+p.getNote());
        }
    }


    //生成流程图---232501
    @RequestMapping("queryProImg")
    public void queryProImg() throws Exception {
        activityService.queryProImg("232501");
    }

    //生成流程图（高亮）---232501
    @RequestMapping("queryProHighLighted")
    public void queryProHighLighted() throws Exception {
        activityService.queryProHighLighted("232501");
    }

    /**
     * 查询流程变量的历史表,可以根据变量名称查询该变量的所有历史信息
     */
    @RequestMapping("findHistoryProcessVariables")
    public void findHistoryProcessVariables() {
        List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询对象
                .variableName("请假天数")
                .list();
        if (list != null && list.size() > 0) {
            for (HistoricVariableInstance hvi : list) {
                System.out.println(hvi.getId() + "     " + hvi.getProcessInstanceId() + "   " + hvi.getVariableName()
                        + "   " + hvi.getVariableTypeName() + "    " + hvi.getValue());
                System.out.println("########################################");
            }
        }

    }


    /**
     * 历史流程实例查询
     * http://blog.csdn.net/luckyzhoustar/article/details/48652783
     */
    @RequestMapping("findHistoricProcessInstance")
    public void findHistoricProcessInstance() {
        // 查询已完成的流程
        List<HistoricProcessInstance> datas = historyService
                .createHistoricProcessInstanceQuery().finished().list();
        System.out.println("使用finished方法：" + datas.size());
        // 根据流程定义ID查询
        datas = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionId("processDefinitionId").list();
        System.out.println("使用processDefinitionId方法： " + datas.size());
        // 根据流程定义key（流程描述文件的process节点id属性）查询
        datas = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey("processDefinitionKey").list();
        System.out.println("使用processDefinitionKey方法： " + datas.size());
        // 根据业务主键查询
        datas = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey("processInstanceBusinessKey").list();
        System.out.println("使用processInstanceBusinessKey方法： " + datas.size());
        // 根据流程实例ID查询
        datas = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId("processInstanceId").list();
        System.out.println("使用processInstanceId方法： " + datas.size());
        // 查询没有完成的流程实例
        historyService.createHistoricProcessInstanceQuery().unfinished().list();
        System.out.println("使用unfinished方法： " + datas.size());
    }

    /**
     * 历史任务查询
     *
     * @throws ParseException
     */
    @RequestMapping("findHistoricTasks")
    public void findHistoricTasks() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //历史数据查询
        List<HistoricTaskInstance> datas = historyService.createHistoricTaskInstanceQuery().finished().list();
        System.out.println("使用finished方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().processDefinitionId("processDefinitionId").list();
        System.out.println("使用processDefinitionId方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().processDefinitionKey("testProcess").list();
        System.out.println("使用processDefinitionKey方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().processDefinitionName("testProcess2").list();
        System.out.println("使用processDefinitionName方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().processFinished().list();
        System.out.println("使用processFinished方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().processInstanceId("processInstanceId").list();
        System.out.println("使用processInstanceId方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().processUnfinished().list();
        System.out.println("使用processUnfinished方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().taskAssignee("crazyit").list();
        System.out.println("使用taskAssignee方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%zy%").list();
        System.out.println("使用taskAssigneeLike方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("usertask1").list();
        System.out.println("使用taskDefinitionKey方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().taskDueAfter(sdf.parse("2020-10-11 06:00:00")).list();
        System.out.println("使用taskDueAfter方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().taskDueBefore(sdf.parse("2022-10-11 06:00:00")).list();
        System.out.println("使用taskDueBefore方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().taskDueDate(sdf.parse("2020-10-11 06:00:00")).list();
        System.out.println("使用taskDueDate方法查询：" + datas.size());

        datas = historyService.createHistoricTaskInstanceQuery().unfinished().list();
        System.out.println("使用unfinished方法查询：" + datas.size());
    }

    /**
     * 历史行为查询
     * 流程在进行过程中，每每走一个节点，都会记录流程节点的信息，包括节点的id，名称、类型、时间等，保存到ACT_HI_ACTINST表中。
     *
     * @throws ParseException
     */
    @RequestMapping("findHistoricActivityInstance")
    public void findHistoricActivityInstance() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //查询数据
        List<HistoricActivityInstance> datas = historyService.createHistoricActivityInstanceQuery()
                .activityId("endevent1").list();
        System.out.println("使用activityId查询：" + datas.size());
        datas = historyService.createHistoricActivityInstanceQuery()
                .activityInstanceId(datas.get(0).getId()).list();
        System.out.println("使用activityInstanceId查询：" + datas.size());
        datas = historyService.createHistoricActivityInstanceQuery()
                .activityType("intermediateSignalCatch").list();
        System.out.println("使用activityType查询：" + datas.size());
        datas = historyService.createHistoricActivityInstanceQuery()
                .executionId("executionId").list();
        System.out.println("使用executionId查询：" + datas.size());
        datas = historyService.createHistoricActivityInstanceQuery().finished().list();
        System.out.println("使用finished查询：" + datas.size());
        datas = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId("processInstanceId").list();
        System.out.println("使用processInstanceId查询：" + datas.size());
        datas = historyService.createHistoricActivityInstanceQuery()
                .taskAssignee("crazyit").list();
        System.out.println("使用taskAssignee查询：" + datas.size());
        datas = historyService.createHistoricActivityInstanceQuery().unfinished().list();
        System.out.println("使用unfinished查询：" + datas.size());
    }

    /**
     * 历史流程明细查询
     * 在流程进行的过程中，会产生许多明细数据，只有将History设置为最高级别的时候，才会被记录到ACT_HI_DETAIL表中。
     *
     * @throws ParseException
     */
    @RequestMapping("findHistoricDetail")
    public void findHistoricDetail() {
        // 查询历史行为
        HistoricActivityInstance act = historyService.createHistoricActivityInstanceQuery()
                .activityName("First Task").finished().singleResult();
        List<HistoricDetail> datas = historyService.createHistoricDetailQuery()
                .activityInstanceId(act.getId()).list();
        System.out.println("使用activityInstanceId方法查询：" + datas.size());
        datas = historyService.createHistoricDetailQuery().excludeTaskDetails().list();
        System.out.println("使用excludeTaskDetails方法查询：" + datas.size());
        datas = historyService.createHistoricDetailQuery().formProperties().list();
        System.out.println("使用formProperties方法查询：" + datas.size());
        datas = historyService.createHistoricDetailQuery().processInstanceId("processInstanceId").list();
        System.out.println("使用processInstanceId方法查询：" + datas.size());
        datas = historyService.createHistoricDetailQuery().taskId("taskId").list();
        System.out.println("使用taskId方法查询：" + datas.size());
        datas = historyService.createHistoricDetailQuery().variableUpdates().list();
        System.out.println("使用variableUpdates方法查询：" + datas.size());
    }

}
