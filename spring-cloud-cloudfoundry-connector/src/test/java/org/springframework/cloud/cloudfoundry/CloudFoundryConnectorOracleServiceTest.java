package org.springframework.cloud.cloudfoundry;

import org.junit.Test;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.OracleServiceInfo;
import org.springframework.cloud.service.common.RelationalServiceInfo;
import org.springframework.cloud.util.UriInfo;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.service.common.OracleServiceInfo.ORACLE_SCHEME;
import static org.springframework.cloud.service.common.RelationalServiceInfo.JDBC_PREFIX;

public class CloudFoundryConnectorOracleServiceTest extends AbstractUserProvidedServiceInfoCreatorTest {

	private static final String INSTANCE_NAME = "database";
	private static final String SERVICE_NAME = "oracle-ups";

	@Test
	public void oracleServiceCreation() {
		when(mockEnvironment.getEnvValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(
						getUserProvidedServicePayload(SERVICE_NAME, hostname, port, username, password, INSTANCE_NAME, ORACLE_SCHEME + ":")));
		List<ServiceInfo> serviceInfos = testCloudConnector.getServiceInfos();

		ServiceInfo info = getServiceInfo(serviceInfos, SERVICE_NAME);
		assertServiceFoundOfType(info, OracleServiceInfo.class);
		assertJdbcUrlEqual(info, ORACLE_SCHEME, INSTANCE_NAME);
		assertUriBasedServiceInfoFields(info, ORACLE_SCHEME, hostname, port, username, password, INSTANCE_NAME);
	}

	@Test
	public void oracleServiceCreationWithSpecialChars() {
		String userWithSpecialChars = "u%u:u+";
		String passwordWithSpecialChars = "p%p:p+";
		when(mockEnvironment.getEnvValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(
						getUserProvidedServicePayload(SERVICE_NAME, hostname, port, userWithSpecialChars, passwordWithSpecialChars, INSTANCE_NAME, ORACLE_SCHEME + ":")));
		List<ServiceInfo> serviceInfos = testCloudConnector.getServiceInfos();

		ServiceInfo info = getServiceInfo(serviceInfos, SERVICE_NAME);
		assertServiceFoundOfType(info, OracleServiceInfo.class);
		assertEquals(getJdbcUrl(hostname, port, INSTANCE_NAME, userWithSpecialChars, passwordWithSpecialChars), ((RelationalServiceInfo)info).getJdbcUrl());
		assertUriBasedServiceInfoFields(info, ORACLE_SCHEME, hostname, port, userWithSpecialChars, passwordWithSpecialChars, INSTANCE_NAME);
	}

	@Test
	public void oracleServiceCreationWithNoUri() {
		when(mockEnvironment.getEnvValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(
						getUserProvidedServicePayloadWithNoUri(SERVICE_NAME, hostname, port, username, password, INSTANCE_NAME)));
		List<ServiceInfo> serviceInfos = testCloudConnector.getServiceInfos();

		ServiceInfo info = getServiceInfo(serviceInfos, SERVICE_NAME);
		assertNotNull(info);
		assertFalse(OracleServiceInfo.class.isAssignableFrom(info.getClass()));  // service was not detected as MySQL
		assertNotNull(info);
	}

	@Test
	public void oracleServiceCreationWithJdbcUrl() {
		when(mockEnvironment.getEnvValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(
						getOracleServicePayloadWithJdbcurl(SERVICE_NAME, hostname, port, username, password, INSTANCE_NAME, ORACLE_SCHEME + ":")));
		List<ServiceInfo> serviceInfos = testCloudConnector.getServiceInfos();

		ServiceInfo info = getServiceInfo(serviceInfos, SERVICE_NAME);
		assertServiceFoundOfType(info, OracleServiceInfo.class);
		assertJdbcUrlEqual(info, ORACLE_SCHEME, INSTANCE_NAME);
		assertUriBasedServiceInfoFields(info, ORACLE_SCHEME, hostname, port, username, password, INSTANCE_NAME);
	}

	protected String getOracleServicePayloadWithJdbcurl(String serviceName, String hostname, int port,
														String user, String password, String name, String scheme) {
		String payload = getRelationalPayload("test-oracle-info-jdbc-url.json", serviceName,
				hostname, port, user, password, name);
		return payload.replace("$scheme", scheme);
	}

	protected String getJdbcUrl(String scheme, String name) {
		return String.format("%s%s:thin:%s/%s@%s:%d/%s", JDBC_PREFIX, scheme, username, password, hostname, port, name);
	}

	private String getJdbcUrl(String hostname, int port, String name, String user, String password) {
		return String.format("%s%s:thin:%s/%s@%s:%d/%s", JDBC_PREFIX, "oracle", UriInfo.urlEncode(user), UriInfo.urlEncode(password), hostname, port, name);
	}
}
