/**
 * Copyright 2013 SPeCS Research Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.mweaver.EventsTest;

import pt.up.fe.specs.util.events.ActionsMap;
import pt.up.fe.specs.util.events.Event;
import pt.up.fe.specs.util.events.EventAction;
import pt.up.fe.specs.util.events.EventReceiverTemplate;

/**
 * @author Joao Bispo
 * 
 */
public abstract class AGear extends EventReceiverTemplate {

    private final ActionsMap actionsMap;

    public AGear() {

	this.actionsMap = newActionsMap();
    }

    /**
     * 
     */
    private ActionsMap newActionsMap() {
	ActionsMap actionsMap = new ActionsMap();

	actionsMap.putAction(WeaverEvent.OnApply, newOnApply());

	return actionsMap;
    }

    public abstract void onApply(String a, String b);

    /**
     * @return
     */
    private EventAction newOnApply() {
	return new EventAction() {

	    @Override
	    public void performAction(Event event) {
		OnApplyData data = (OnApplyData) event.getData();
		onApply(data.a, data.b);
	    }
	};
    }

    /* (non-Javadoc)
     * @see pt.up.fe.specs.util.Events.EventReceiverTemplate#getActionsMap()
     */
    @Override
    protected ActionsMap getActionsMap() {
	return actionsMap;
    }

}
