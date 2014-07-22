/**
 * Copyright 1996-2014 FoxBPM ORG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author ych
 */
package org.foxbpm.engine.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.foxbpm.engine.datavariable.VariableInstance;
import org.foxbpm.engine.impl.identity.Authentication;
import org.foxbpm.engine.impl.model.DeploymentBuilderImpl;
import org.foxbpm.engine.impl.task.command.ExpandTaskCommand;
import org.foxbpm.engine.impl.util.ReflectUtil;
import org.foxbpm.engine.repository.ProcessDefinition;
import org.foxbpm.engine.repository.ProcessDefinitionQuery;
import org.foxbpm.engine.runtime.ProcessInstance;
import org.foxbpm.engine.task.Task;
import org.foxbpm.engine.test.AbstractFoxBpmTestCase;
import org.foxbpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class ModelServiceTest extends AbstractFoxBpmTestCase {

	/**
	 * 测试通过用户id获取启动流程
	 * <p>1.使用场景：通过用户id获取启动流程</p>
	 * <p>2.预置条件：已存在发布的流程<p>
	 * <p>3.处理过程：在流程人工任务上配置好任务分配,然后通过用户id获取启动流程</p>
	 * <p>4.测试用例：预置流程定义人工任务分配并且发布,通过用户id获取启动流程</p>
	 */
	@Test
	@Deployment(resources = { "org/foxbpm/engine/test/impl/bpmn/deployer/GetStartProcessByUserId_1.bpmn" })
	public void testGetStartProcessByUserId() {
		Authentication.setAuthenticatedUserId("admin");
		runtimeService.startProcessInstanceByKey("GetStartProcessByUserId_1");
		Object result = modelService.getStartProcessByUserId("admin");
		assertNotNull(result);
	}

	/**
	 * 测试发布流程定义
	 * <p>1.使用场景：发布流程定义</p>
	 * <p>2.预置条件：1.只存在bpmn文件,2.同时存在bpmn和png文件,3.存在发布Id(bpmn,png),(bpmn)<p>
	 * <p>3.处理过程：通过获取bpmn、png、发布Id来发布流程</p>
	 * <p>4.测试用例：1.只发布bpmn,2.同时发布bpmn和png,3.存在发布Id</p>
	 */
	@Test
	public void testDeploy() {
		ZipInputStream zipInputStream = null;
		DeploymentBuilderImpl deploymentBuilder = null;
		SqlRowSet rowSet = null;
		int count = 0;
		String updateDeployId = null;
		List<String> resourceNames = null;
		String version = null;
		//1.自动生成png
		// 1.只存在bpmn文件,
		try {
			deploymentBuilder = (DeploymentBuilderImpl) modelService.createDeployment();
			zipInputStream = new ZipInputStream(ReflectUtil.getResourceAsStream("org/foxbpm/engine/test/impl/bpmn/deployer/TestDeployment3_nopng.zip"));
			deploymentBuilder.addZipInputStream(zipInputStream);
			String deployId = modelService.deploy(deploymentBuilder).getId();
			updateDeployId = deployId;

			// 查流程定义
			rowSet = jdbcTemplate.queryForRowSet("select * from FOXBPM_DEF_PROCESSDEFINITION where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 1, count);
			// 查发布
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_deployment where id='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 1, count);
			// 查资源
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_bytearray where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			resourceNames = new ArrayList<String>();
			while (rowSet.next()) {
				count++;
				resourceNames.add(rowSet.getString("name"));
			}
			assertEquals("发布流程出现错误", 2, count);
			if (!resourceNames.contains("TestDeployment3_1.bpmn") || !resourceNames.contains("TestDeployment3_1.png")) {
				throw new RuntimeException("发布流程出现错误");
			}
		} finally {
			if (null != zipInputStream) {
				try {
					zipInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		//2. 测试版本号
		// 1.只存在bpmn文件
		try {
			deploymentBuilder = (DeploymentBuilderImpl) modelService.createDeployment();
			zipInputStream = new ZipInputStream(ReflectUtil.getResourceAsStream("org/foxbpm/engine/test/impl/bpmn/deployer/TestDeployment3_nopng.zip"));
			deploymentBuilder.addZipInputStream(zipInputStream);
			String deployId = modelService.deploy(deploymentBuilder).getId();
			updateDeployId = deployId;

			// 查流程定义
			rowSet = jdbcTemplate.queryForRowSet("select * from FOXBPM_DEF_PROCESSDEFINITION where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
				version = rowSet.getString("version");
			}
			assertEquals("发布流程出现错误", 1, count);
			assertEquals("发布流程出现错误", "2", version);
			// 查发布
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_deployment where id='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 1, count);
			// 查资源
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_bytearray where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			resourceNames = new ArrayList<String>();
			while (rowSet.next()) {
				count++;
				resourceNames.add(rowSet.getString("name"));
			}
			assertEquals("发布流程出现错误", 2, count);
			if (!resourceNames.contains("TestDeployment3_1.bpmn") || !resourceNames.contains("TestDeployment3_1.png")) {
				throw new RuntimeException("发布流程出现错误");
			}
		} finally {
			if (null != zipInputStream) {
				try {
					zipInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// 3.更新bpmn png
		try {
			deploymentBuilder = (DeploymentBuilderImpl) modelService.createDeployment();
			zipInputStream = new ZipInputStream(ReflectUtil.getResourceAsStream("org/foxbpm/engine/test/impl/bpmn/deployer/TestDeployment3_updatepng.zip"));
			deploymentBuilder.addZipInputStream(zipInputStream);
			deploymentBuilder.updateDeploymentId(updateDeployId);
			modelService.deploy(deploymentBuilder);
			// 查流程定义
			rowSet = jdbcTemplate.queryForRowSet("select * from FOXBPM_DEF_PROCESSDEFINITION where DEPLOYMENT_ID='" + updateDeployId + "'");
			count = 0;

			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 1, count);
			// 查发布
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_deployment where id='" + updateDeployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 1, count);
			// 查资源
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_bytearray where DEPLOYMENT_ID='" + updateDeployId + "'");
			count = 0;
			resourceNames = new ArrayList<String>();
			while (rowSet.next()) {
				count++;
				resourceNames.add(rowSet.getString("name"));
			}
			assertEquals("发布流程出现错误", 2, count);
			if (!resourceNames.contains("TestDeployment3_1.bpmn") || !resourceNames.contains("TestDeployment3_1.png")) {
				throw new RuntimeException("发布流程出现错误");
			}
		} finally {
			if (null != zipInputStream) {
				try {
					zipInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// 4.更新只有bpmn,自动生成png
		try {
			deploymentBuilder = (DeploymentBuilderImpl) modelService.createDeployment();
			zipInputStream = new ZipInputStream(ReflectUtil.getResourceAsStream("org/foxbpm/engine/test/impl/bpmn/deployer/TestDeployment3_updatepng.zip"));
			deploymentBuilder.addZipInputStream(zipInputStream);
			deploymentBuilder.updateDeploymentId(updateDeployId);
			modelService.deploy(deploymentBuilder);
			// 查流程定义
			rowSet = jdbcTemplate.queryForRowSet("select * from FOXBPM_DEF_PROCESSDEFINITION where DEPLOYMENT_ID='" + updateDeployId + "'");
			count = 0;

			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 1, count);
			// 查发布
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_deployment where id='" + updateDeployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 1, count);
			// 查资源
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_bytearray where DEPLOYMENT_ID='" + updateDeployId + "'");
			count = 0;
			resourceNames = new ArrayList<String>();
			while (rowSet.next()) {
				count++;
				resourceNames.add(rowSet.getString("name"));
			}
			assertEquals("发布流程出现错误", 2, count);
			if (!resourceNames.contains("TestDeployment3_1.bpmn") || !resourceNames.contains("TestDeployment3_1.png")) {
				throw new RuntimeException("发布流程出现错误");
			}
		} finally {
			if (null != zipInputStream) {
				try {
					zipInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//5.正常发布
		// 5.同时存在bpmn和png文件
		try {
			deploymentBuilder = (DeploymentBuilderImpl) modelService.createDeployment();
			zipInputStream = new ZipInputStream(ReflectUtil.getResourceAsStream("org/foxbpm/engine/test/impl/bpmn/deployer/deployer_normal.zip"));
			deploymentBuilder.addZipInputStream(zipInputStream);
			String deployId = modelService.deploy(deploymentBuilder).getId();
			// 查流程定义
			rowSet = jdbcTemplate.queryForRowSet("select * from FOXBPM_DEF_PROCESSDEFINITION where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 1, count);
			// 查发布
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_deployment where id='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 1, count);
			// 查资源
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_bytearray where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			resourceNames = new ArrayList<String>();
			while (rowSet.next()) {
				count++;
				resourceNames.add(rowSet.getString("name"));
			}
			assertEquals("发布流程出现错误", 2, count);
			if (!resourceNames.contains("TestTestDeployment2_1.bpmn") || !resourceNames.contains("TestTestDeployment2_1.png")) {
				throw new RuntimeException("发布流程出现错误");
			}
		} finally {
			if (null != zipInputStream) {
				try {
					zipInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 测试删除发布(涉及数据级联删除)
	 * <p>1.使用场景：删除发布</p>
	 * <p>2.预置条件：存在已发布流程定义<p>
	 * <p>3.处理过程：1.首先发布一个流程定义包括（驱动流程至结束、涉及变量）,2.然后通过发布id来删除</p>
	 * <p>4.测试用例：1.用已存在的发布id来删除发布,2.需要对与流程定义相关的所有数据也被清空</p>
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteDeployment() {
		ZipInputStream zipInputStream = null;
		DeploymentBuilderImpl deploymentBuilder = null;
		SqlRowSet rowSet = null;
		int count = 0;
		try {
			// 发布流程定义
			deploymentBuilder = (DeploymentBuilderImpl) modelService.createDeployment();
			zipInputStream = new ZipInputStream(ReflectUtil.getResourceAsStream("org/foxbpm/engine/test/impl/bpmn/deployer/testDeleteDeployment_1.zip"));
			deploymentBuilder.addZipInputStream(zipInputStream);
			String deployId = modelService.deploy(deploymentBuilder).getId();

			/************************** 流程驱动 **********************************/
			jdbcTemplate.execute("insert into au_userInfo(userId,USERNAME) VALUES ('c','管理员4')");

			Authentication.setAuthenticatedUserId("admin");
			// 启动
			ProcessInstance pi = runtimeService.startProcessInstanceByKey("testDeleteDeployment_1");
			// 驱动流程
			ExpandTaskCommand expandTaskCommand = null;
			Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskNotEnd().singleResult();
			expandTaskCommand = new ExpandTaskCommand();
			expandTaskCommand.setProcessDefinitionKey("testDeleteDeployment_1");
			expandTaskCommand.setTaskCommandId("HandleCommand_1");
			expandTaskCommand.setTaskId(task.getId());
			expandTaskCommand.setCommandType("HandleCommand_1");
			expandTaskCommand.setBusinessKey("bizKey");
			expandTaskCommand.setInitiator("a");
			/************************** 发布后数据查看 **********************************/
			// 查看变量
			VariableInstance vi = this.runtimeService.createVariableQuery().processInstanceId(pi.getId()).addVariableKey("data").singleResult();
			List<Map<String, Object>> dataValue = null;
			if (null != vi) {
				dataValue = (List<Map<String, Object>>) vi.getValueObject();
			}
			assertNotNull(dataValue);
			/************************** 删除发布前 **********************************/
			assertNotNull(pi.getId());
			List<String> taskIds = new ArrayList<String>();
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_run_task where PROCESSINSTANCE_ID='" + pi.getId() + "'");
			while (rowSet.next()) {
				taskIds.add(rowSet.getString("ID"));
			}
			assertNotNull("删除task出现错误", taskIds);
			// 令牌
			List<String> tokenIds = new ArrayList<String>();
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_run_token where PROCESSINSTANCE_ID='" + pi.getId() + "'");
			while (rowSet.next()) {
				tokenIds.add(rowSet.getString("ID"));
			}
			assertNotNull("删除token出现错误", tokenIds);

			// 变量
			List<String> variableIds = new ArrayList<String>();
			count = 0;
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_run_variable t WHERE  t.PROCESSINSTANCE_ID ='" + pi.getId() + "'");
			while (rowSet.next()) {
				variableIds.add(rowSet.getString("ID"));
			}
			assertNotNull("删除variable出现错误", variableIds);

			// 查流程定义
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_processdefinition where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("删除发布出现错误", 1, count);
			// 查发布
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_deployment where id='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("删除发布出现错误", 1, count);
			// 查资源
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_bytearray where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("删除发布出现错误", 2, count);

			/************************** 删除发布 **********************************/
			// 删除流程定义
			modelService.deleteDeployment(deployId);
			/************************** 删除发布后数据查看 **********************************/
			// 令牌
			count = 0;
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_run_token t WHERE  t.PROCESSINSTANCE_ID ='" + pi.getId() + "'");
			while (rowSet.next()) {
				count++;
			}
			assertEquals("删除token出现错误", 0, count);
			// 任务
			count = 0;
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_run_task t WHERE  t.PROCESSINSTANCE_ID ='" + pi.getId() + "'");
			while (rowSet.next()) {
				count++;
			}
			assertEquals("删除task出现错误", 0, count);
			// 变量
			count = 0;
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_run_variable t WHERE  t.PROCESSINSTANCE_ID ='" + pi.getId() + "'");
			while (rowSet.next()) {
				count++;
			}
			assertEquals("删除variable出现错误", 0, count);
			for (String id : taskIds) {
				// 任务候选人
				count = 0;
				rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_run_taskidentitylink t WHERE  t.TASK_ID ='" + id + "'");
				while (rowSet.next()) {
					count++;
				}
				assertEquals("删除候选人出现错误", 0, count);
			}

			// 查流程定义
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_processdefinition where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("删除发布出现错误", 0, count);
			// 查发布
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_deployment where id='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("删除发布出现错误", 0, count);
			// 查资源
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_bytearray where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("删除发布出现错误", 0, count);
		} finally {
			if (null != zipInputStream) {
				try {
					zipInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 测试创建发布
	 * <p>1.使用场景：发布</p>
	 * <p>2.预置条件：存在待发布流程定义<p>
	 * <p>3.处理过程：1.首先发布一个流程定义</p>
	 * <p>4.测试用例：创建一个发布用来发布一个流程定义,查看是否发布成功</p>
	 */
	@Test
	public void createDeployment() {
		ZipInputStream zipInputStream = null;
		DeploymentBuilderImpl deploymentBuilder = null;
		SqlRowSet rowSet = null;
		int count = 0;
		try {
			// 发布流程定义
			deploymentBuilder = (DeploymentBuilderImpl) modelService.createDeployment();
			zipInputStream = new ZipInputStream(ReflectUtil.getResourceAsStream("org/foxbpm/engine/test/impl/bpmn/deployer/TestDeployment3_nopng.zip"));
			deploymentBuilder.addZipInputStream(zipInputStream);
			String deployId = deploymentBuilder.deploy().getId();
			// 删除流程定义
			modelService.deleteDeployment(deployId);
			// 查流程定义
			rowSet = jdbcTemplate.queryForRowSet("select * from FOXBPM_DEF_PROCESSDEFINITION where DEPLOYMENT_ID='" + deployId + "'");
			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 0, count);
			// 查发布
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_deployment where id='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 0, count);
			// 查资源
			rowSet = jdbcTemplate.queryForRowSet("select * from foxbpm_def_bytearray where DEPLOYMENT_ID='" + deployId + "'");
			count = 0;
			while (rowSet.next()) {
				count++;
			}
			assertEquals("发布流程出现错误", 0, count);
		} finally {
			if (null != zipInputStream) {
				try {
					zipInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 测试通过创建的流程定义查询
	 * <p>1.使用场景：流程定义查询</p>
	 * <p>2.预置条件：存在已发发布流程定义<p>
	 * <p>3.处理过程：1.首先创建一个流程定义查询对象,2.通过该对象设置流程查询参数</p>
	 * <p>4.测试用例：1.发布一条流程定义,2.根据该定义key进行查询,查看查询结果</p>
	 */
	@Test
	@Deployment(resources = { "org/foxbpm/engine/test/impl/bpmn/deployer/TestQuery_1.bpmn" })
	public void testCreateProcessDefinitionQuery() {
		Authentication.setAuthenticatedUserId("admin");
		runtimeService.startProcessInstanceByKey("TestQuery_1");
		ProcessDefinitionQuery processQuery = modelService.createProcessDefinitionQuery();
		processQuery.processDefinitionKey("TestQuery_1").orderByProcessDefinitionKey().asc();
		List<ProcessDefinition> process = processQuery.listPage(0, 10);
		assertNotNull("创建的流程定义查询失败", process);
	}

	/**
	 * 测试根据流程定义id获取流程图节点信息
	 * <p>1.使用场景：获取流程图节点信息</p>
	 * <p>2.预置条件：存在已发发布流程定义<p>
	 * <p>3.处理过程：1.通过流程定义id查询流程图节点信息</p>
	 * <p>4.测试用例：1.发布一条流程定义,2.根据该定义key进行查询流程图节点信息</p>
	 */
	@Test
	@Deployment(resources = { "org/foxbpm/engine/test/impl/bpmn/deployer/TestQuery_1.bpmn" })
	public void testGetFlowGraphicsElementPositionById() {
		Authentication.setAuthenticatedUserId("admin");
		runtimeService.startProcessInstanceByKey("TestQuery_1");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("TestQuery_1");
		Map<String, Map<String, Object>> result = modelService.getFlowGraphicsElementPositionById(pi.getProcessDefinitionId());
		assertNotNull("根据流程定义Id获取流程图节点失败", result);
	}

	/**
	 * 测试根据流程定义key获取流程图节点信息
	 * <p>1.使用场景：获取流程图节点信息</p>
	 * <p>2.预置条件：存在已发发布流程定义<p>
	 * <p>3.处理过程：1.通过流程定义key查询流程图节点信息</p>
	 * <p>4.测试用例：1.发布一条流程定义,2.根据该定义key进行查询流程图节点信息</p>
	 */
	@Test
	@Deployment(resources = { "org/foxbpm/engine/test/impl/bpmn/deployer/TestQuery_1.bpmn" })
	public void testGetFlowGraphicsElementPositionByKey() {
		Authentication.setAuthenticatedUserId("admin");
		runtimeService.startProcessInstanceByKey("TestQuery_1");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("TestQuery_1");
		Map<String, Map<String, Object>> result = modelService.getFlowGraphicsElementPositionByKey(pi.getProcessDefinitionKey());
		assertNotNull("根据流程定义Key获取流程图节点失败", result);
	}
	
	/**
	 * 测试根据流程定义id获取流程图流
	 * <p>1.使用场景：获取流程图节点信息</p>
	 * <p>2.预置条件：存在已发发布流程定义<p>
	 * <p>3.处理过程：1.通过流程定义id查询流程图流</p>
	 * <p>4.测试用例：1.发布一条流程定义,2.根据该定义key进行查询流程图流</p>
	 */
	@Test
	@Deployment(resources = { "org/foxbpm/engine/test/impl/bpmn/deployer/TestQuery_1.bpmn" })
	public void testGetFlowGraphicsImgStreamByDefId() {
		Authentication.setAuthenticatedUserId("admin");
		runtimeService.startProcessInstanceByKey("TestQuery_1");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("TestQuery_1");
		Object result = modelService.GetFlowGraphicsImgStreamByDefId(pi.getProcessDefinitionId());
		assertNotNull("根据流程定义Id获取流程图流点失败", result);
	}

	/**
	 * 测试根据流程定义key获取流程图流
	 * <p>1.使用场景：获取流程图节点信息</p>
	 * <p>2.预置条件：存在已发发布流程定义<p>
	 * <p>3.处理过程：1.通过流程定义key查询流程图流</p>
	 * <p>4.测试用例：1.发布一条流程定义,2.根据该定义key进行查询流程图流</p>
	 */
	@Test
	@Deployment(resources = { "org/foxbpm/engine/test/impl/bpmn/deployer/TestQuery_1.bpmn" })
	public void testGetFlowGraphicsImgStreamByDefKey() {
		Authentication.setAuthenticatedUserId("admin");
		runtimeService.startProcessInstanceByKey("TestQuery_1");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("TestQuery_1");
		Object result = modelService.GetFlowGraphicsImgStreamByDefKey(pi.getProcessDefinitionKey());
		assertNotNull("根据流程定义Key获取流程图流失败", result);
	}

	/**
	 * 测试根据流程定义(key和version),(id)获取流程定义实例
	 * <p>1.使用场景：获取流程定义实例</p>
	 * <p>2.预置条件：存在已发发布流程定义<p>
	 * <p>3.处理过程：1.通过流程定义(key和version),(id)获取流程定义实例</p>
	 * <p>4.测试用例：1.发布一条流程定义,2.根据该定义(key和version),(id)获取流程定义实例</p>
	 */
	@Test
	@Deployment(resources = { "org/foxbpm/engine/test/impl/bpmn/deployer/TestQuery_1.bpmn" })
	public void testGetProcessDefinition() {
		Authentication.setAuthenticatedUserId("admin");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("TestQuery_1");
		ProcessDefinition pd = modelService.getProcessDefinition(pi.getProcessDefinitionKey(), 1);
		assertNotNull("通过key和version获取流程定义失败", pd);
		pd = modelService.getProcessDefinition(pi.getProcessDefinitionId());
		assertNotNull("通过id获取流程定义失败", pd);
	}

	/**
	 * 测试根据流程定义id获取svg
	 * <p>1.使用场景：获取流程定义实例</p>
	 * <p>2.预置条件：存在已发发布流程定义<p>
	 * <p>3.处理过程：1.通过流程定义id获取svg</p>
	 * <p>4.测试用例：1.发布一条流程定义,2.根据该定义id获取svg</p>
	 */
	@Test
	@Deployment(resources = { "org/foxbpm/engine/test/impl/bpmn/deployer/TestQuery_1.bpmn" })
	public void testGetProcessDefinitionSVG() {
		Authentication.setAuthenticatedUserId("admin");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("TestQuery_1");
		String svg = modelService.getProcessDefinitionSVG(pi.getProcessDefinitionId());
		System.out.println("svg=" + svg);
		assertNotNull("获取流程定义svg失败", svg);
	}

	/**
	 * 测试判断用户是否有发起权限
	 * <p>1.使用场景：判断用户是否有权限发起流程</p>
	 * <p>2.预置条件：存在已发布流程定义,并且在人工任务上设置好分配任务<p>
	 * <p>3.处理过程：1.通过流程定义id和用户名来判断</p>
	 * <p>4.测试用例：1.发布一条流程定义,2.启动该流程,3.根据任务分配,通过流程定义和用户id来判断是否有发起权限</p>
	 */
	@Test
	@Deployment(resources = { "org/foxbpm/engine/test/impl/bpmn/deployer/VerifyStartProcessByUserId_1.bpmn" })
	public void testVerifyStartProcessByUserId() {
		Authentication.setAuthenticatedUserId("admin");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("VerifyStartProcessByUserId_1");
		boolean flag = modelService.verifyStartProcessByUserId("admin", pi.getProcessDefinitionId());
	    assertEquals("流程权限判断出现错误",true, flag);
	}

	/**
	 * 测试通过发布id和资源名称获取资源流
	 * <p>1.使用场景：通过发布id和资源名称获取资源流</p>
	 * <p>2.预置条件：存在已发布流程定义<p>
	 * <p>3.处理过程：通过发布id和资源名称获取资源流</p>
	 * <p>4.测试用例：1.发布流程2.通过发布id和资源名称获取资源流</p>
	 */
	@Test
	public void testGetResourceByDeployIdAndName() {
		ZipInputStream zipInputStream = null;
		DeploymentBuilderImpl deploymentBuilder = null;
		try {
			// 发布流程定义
			deploymentBuilder = (DeploymentBuilderImpl) modelService.createDeployment();
			zipInputStream = new ZipInputStream(ReflectUtil.getResourceAsStream("org/foxbpm/engine/test/impl/bpmn/deployer/TestDeployment3_nopng.zip"));
			deploymentBuilder.addZipInputStream(zipInputStream);
			String deployId = modelService.deploy(deploymentBuilder).getId();
			Object result = modelService.getResourceByDeployIdAndName(deployId, "TestDeployment3_1.png");
			assertNotNull("通过发布id和资源名称获取资源流失败", result);
		} finally {
			if (null != zipInputStream) {
				try {
					zipInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 测试获取最新发布流程定义
	 * <p>1.使用场景：获取最新发布流程定义</p>
	 * <p>2.预置条件：存在已发布流程定义<p>
	 * <p>3.处理过程：通过流程定义key，获取最新发布流程定义</p>
	 * <p>4.测试用例：1.发布流程2.更新流程</p>
	 */
	@Test
	@Deployment(resources = { "org/foxbpm/engine/test/impl/bpmn/deployer/GetLatestProcessDefinie_1.bpmn" })
	public void testGetLatestProcessDefinition() {
		String processDefinitionKey = "GetLatestProcessDefinie_1";
		ProcessDefinition pd = modelService.getLatestProcessDefinition(processDefinitionKey);
		assertNotNull(pd);
	}
}
