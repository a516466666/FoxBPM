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
 * @author kenshin
 */
package org.foxbpm.connector.actorconnector.SelectDeptActorConnector;

import java.util.List;

import org.foxbpm.engine.exception.FoxBPMConnectorException;
import org.foxbpm.engine.impl.connector.ActorConnectorHandler;
import org.foxbpm.engine.impl.entity.GroupEntity;
import org.foxbpm.engine.impl.util.AssigneeUtil;
import org.foxbpm.engine.impl.util.StringUtil;
import org.foxbpm.engine.task.DelegateTask;

public class SelectDeptActorConnector extends ActorConnectorHandler {

	private static final long serialVersionUID = 1L;

	private java.lang.String deptId;

	public void setDeptId(java.lang.String deptId) {
		this.deptId = deptId;
	}

	@Override
	public void assign(DelegateTask task) throws Exception {

		if (StringUtil.isEmpty(StringUtil.trim(deptId))) {
			throw new FoxBPMConnectorException("deptId is null !");
		}
		List<String> deptList = AssigneeUtil.executionExpressionObj(deptId);
		for (String deptId : deptList) {
			GroupEntity group = new GroupEntity(deptId, "dept");
			task.addCandidateGroupEntity(group);
		}
	}
}