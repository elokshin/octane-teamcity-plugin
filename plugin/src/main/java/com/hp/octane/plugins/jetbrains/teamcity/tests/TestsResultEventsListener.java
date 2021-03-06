/*
 *     2017 EntIT Software LLC, a Micro Focus company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hp.octane.plugins.jetbrains.teamcity.tests;

import com.hp.octane.integrations.OctaneSDK;
import jetbrains.buildServer.serverSide.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by lev on 06/01/2016.
 */

public class TestsResultEventsListener extends BuildServerAdapter {
	private TestsResultEventsListener(SBuildServer server) {
		server.addListener(this);
	}

	@Override
	public void buildFinished(@NotNull SRunningBuild build) {
		BuildStatistics stats = build.getBuildStatistics(new BuildStatisticsOptions());
		if (!stats.getTests(null, BuildStatistics.Order.NATURAL_ASC).isEmpty()) {
			OctaneSDK.getClients().forEach(client -> client.getTestsService().enqueuePushTestsResult(build.getBuildTypeExternalId(),
					String.valueOf(build.getBuildId()), build.getBuildTypeExternalId()));
		}
	}
}
