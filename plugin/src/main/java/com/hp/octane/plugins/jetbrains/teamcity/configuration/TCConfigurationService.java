package com.hp.octane.plugins.jetbrains.teamcity.configuration;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by lazara.
 * Created by gadiel.
 */

//  [YG] TODO: move the storage of the configuration to permanent location
public class TCConfigurationService {
	private static final Logger logger = Logger.getLogger(TCConfigurationService.class.getName());

	@Autowired
	private SBuildServer buildServer;
	@Autowired
	private NGAPlugin ngaPlugin;

	public String checkConfiguration(NGAConfiguration ngaConfiguration) {
		String resultMessage;

		try {
			NGAResponse result = SDKManager.getService(ConfigurationService.class).validateConfiguration(ngaConfiguration);
			if (result.getStatus() == HttpStatus.SC_OK) {
				resultMessage = "Connection succeeded";
			} else if (result.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
				resultMessage = "Authentication failed";
			} else if (result.getStatus() == HttpStatus.SC_FORBIDDEN) {
				resultMessage = ngaConfiguration.getApiKey() + " not authorized to shared space " + ngaConfiguration.getSharedSpace();
			} else if (result.getStatus() == HttpStatus.SC_NOT_FOUND) {
				resultMessage = "Shared space " + ngaConfiguration.getSharedSpace() + " not exists";
			} else {
				resultMessage = "Validation failed for unknown reason; status code: " + result.getStatus();
			}
		} catch (IOException ioe) {
			resultMessage = "Connection failed: " + ioe.getMessage();
		}

		return resultMessage;
	}

	public NGAConfigStructure readConfig() {
		try {
			JAXBContext context = JAXBContext.newInstance(NGAConfigStructure.class);
			Unmarshaller un = context.createUnmarshaller();
			return (NGAConfigStructure) un.unmarshal(new File(getConfigResourceLocation()));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void saveConfig(NGAConfigStructure config) {
		try {
			JAXBContext context = JAXBContext.newInstance(NGAConfigStructure.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(config, new File(getConfigResourceLocation()));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	private String getConfigResourceLocation() {
		return buildServer.getServerRootPath() + ngaPlugin.getDescriptor().getPluginResourcesPath("ConfigFile.xml");
	}
}