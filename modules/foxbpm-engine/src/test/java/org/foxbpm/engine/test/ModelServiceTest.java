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
package org.foxbpm.engine.test;

import java.util.List;

import org.foxbpm.engine.ProcessEngine;
import org.foxbpm.engine.ProcessEngineManagement;
import org.foxbpm.engine.RuntimeService;
import org.foxbpm.engine.impl.entity.ProcessInstanceEntity;
import org.foxbpm.engine.query.NativeTaskQuery;
import org.foxbpm.engine.runtime.ProcessInstance;
import org.foxbpm.engine.task.Task;

public class ModelServiceTest extends AbstractFoxBpmTestCase {
	
	public void testStartProcessById(){
		ProcessEngine processEngine = ProcessEngineManagement.getDefaultProcessEngine();
		
		RuntimeService runtimeService=processEngine.getRuntimeService();
		ProcessInstance processInstance=runtimeService.startProcessInstanceById("1","bizkeyValue");

		NativeTaskQuery nativeTaskQuery=processEngine.getTaskService().createNativeTaskQuery();
		List<Task> tasks = nativeTaskQuery.sql("SELECT * FROM FOXBPM_RUN_TASK").list();
		
		ProcessInstanceEntity processInstanceEntity=(ProcessInstanceEntity)processInstance;
		
		
		
		runtimeService.signal(processInstanceEntity.getRootTokenId());
		
		
		tasks = nativeTaskQuery.sql("SELECT * FROM FOXBPM_RUN_TASK").list();
		
		
		assertNotNull(processInstance);
	}
}
