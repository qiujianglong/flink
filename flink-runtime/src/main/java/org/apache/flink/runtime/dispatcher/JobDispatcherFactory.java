/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.dispatcher;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.blob.BlobServer;
import org.apache.flink.runtime.entrypoint.ClusterEntrypoint;
import org.apache.flink.runtime.entrypoint.component.JobGraphRetriever;
import org.apache.flink.runtime.heartbeat.HeartbeatServices;
import org.apache.flink.runtime.highavailability.HighAvailabilityServices;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.metrics.groups.JobManagerMetricGroup;
import org.apache.flink.runtime.resourcemanager.ResourceManagerGateway;
import org.apache.flink.runtime.rpc.FatalErrorHandler;
import org.apache.flink.runtime.rpc.RpcService;

import javax.annotation.Nullable;

import static org.apache.flink.runtime.entrypoint.ClusterEntrypoint.EXECUTION_MODE;

/**
 * {@link DispatcherFactory} which creates a {@link MiniDispatcher}.
 */
public class JobDispatcherFactory implements DispatcherFactory<MiniDispatcher> {

	private final JobGraphRetriever jobGraphRetriever;

	public JobDispatcherFactory(JobGraphRetriever jobGraphRetriever) {
		this.jobGraphRetriever = jobGraphRetriever;
	}

	@Override
	public MiniDispatcher createDispatcher(
			Configuration configuration,
			RpcService rpcService,
			HighAvailabilityServices highAvailabilityServices,
			ResourceManagerGateway resourceManagerGateway,
			BlobServer blobServer,
			HeartbeatServices heartbeatServices,
			JobManagerMetricGroup jobManagerMetricGroup,
			@Nullable String metricQueryServicePath,
			ArchivedExecutionGraphStore archivedExecutionGraphStore,
			FatalErrorHandler fatalErrorHandler,
			HistoryServerArchivist historyServerArchivist) throws Exception {
		final JobGraph jobGraph = jobGraphRetriever.retrieveJobGraph(configuration);

		final String executionModeValue = configuration.getString(EXECUTION_MODE);

		final ClusterEntrypoint.ExecutionMode executionMode = ClusterEntrypoint.ExecutionMode.valueOf(executionModeValue);

		return new MiniDispatcher(
			rpcService,
			Dispatcher.DISPATCHER_NAME,
			configuration,
			highAvailabilityServices,
			resourceManagerGateway,
			blobServer,
			heartbeatServices,
			jobManagerMetricGroup,
			metricQueryServicePath,
			archivedExecutionGraphStore,
			DefaultJobManagerRunnerFactory.INSTANCE,
			fatalErrorHandler,
			historyServerArchivist,
			jobGraph,
			executionMode);
	}
}
