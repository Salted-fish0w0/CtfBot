package org.hackbug.saltedfish.fishbot.ctf.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DockerManager {
	private final DockerClient dc;
	public DockerManager() {
		DockerClientConfig custom = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("unix:///var/run/docker.sock")
				.build();
		dc = DockerClientBuilder.getInstance(custom).build();
	}

	public String buildContainer(String imageName, int port) {
		ExposedPort exposedPort = ExposedPort.tcp(port);
		Ports portBindings = new Ports();
		portBindings.bind(exposedPort, Ports.Binding.bindPort(port));
		CreateContainerResponse response = dc.createContainerCmd(imageName)
				.withCmd("/bin/bash")
				.withTty(true)
				.withStdinOpen(true)
				.withHostConfig(HostConfig.newHostConfig().withPortBindings(portBindings))
				.withExposedPorts(exposedPort)
				.exec();

		return response.getId();
	}

	public void uploadArchive(String container, File archiveFile) {
		try {
			dc.copyArchiveToContainerCmd(container).withTarInputStream(new FileInputStream(archiveFile)).withRemotePath("/").exec();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void startContainer(String container) {
		dc.startContainerCmd(container)
				.exec();
	}

	public void stopContainer(String container) {
		dc.stopContainerCmd(container).exec();
	}

	public void destroyContainer(String container) {
		dc.removeContainerCmd(container).exec();
	}

	public void executeCommand(String container, String command) {
		ExecCreateCmdResponse resp = dc.execCreateCmd(container).withCmd("sh", command).exec();
		dc.execStartCmd(resp.getId()).withDetach(true).withTty(true).exec(new ResultCallback.Adapter<>());
	}
}
