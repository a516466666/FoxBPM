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
package org.foxbpm.engine.datavariable;

import org.foxbpm.engine.query.Query;

public interface VariableQuery extends Query<VariableQuery, VariableInstance> {

	VariableQuery id(String id);

	VariableQuery processInstanceId(String processInstanceId);

	VariableQuery processDefinitionId(String processDefinitionId);

	VariableQuery processDefinitionKey(String processDefinitionKey);

	VariableQuery taskId(String taskId);

	VariableQuery tokenId(String tokenId);

	VariableQuery addVariableKey(String key);
	
	VariableQuery nodeId(String nodeId);

}
