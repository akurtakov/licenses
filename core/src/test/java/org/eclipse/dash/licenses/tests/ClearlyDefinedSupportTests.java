/*************************************************************************
 * Copyright (c) 2019,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedContentData;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.tests.util.TestContext;
import org.eclipse.dash.licenses.util.JsonUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClearlyDefinedSupportTests {

	@Test
	void testMatchApproved() {
		List<IContentData> results = new ArrayList<>();
		new TestContext().getClearlyDefinedService().queryLicenseData(
				Collections.singleton(ContentId.getContentId("npm/npmjs/-/write/1.0.3")), data -> results.add(data));

		assertEquals(1, results.size());

		IContentData write = results.get(0);
		assertEquals("npm/npmjs/-/write/1.0.3", write.getId().toString());
		assertEquals("clearlydefined", write.getAuthority());
		assertEquals("MIT", write.getLicense());
		assertEquals("https://clearlydefined.io/definitions/npm/npmjs/-/write/1.0.3", write.getUrl());
		assertEquals(94, write.getScore());
		assertEquals(LicenseSupport.Status.Approved, write.getStatus());
	}

	@Test
	void testMatchRestricted() {
		List<IContentData> results = new ArrayList<>();
		new TestContext().getClearlyDefinedService().queryLicenseData(
				Collections.singleton(ContentId.getContentId("npm/npmjs/@yarnpkg/lockfile/1.1.0")),
				data -> results.add(data));

		assertEquals(1, results.size());

		IContentData write = results.get(0);
		assertEquals("npm/npmjs/@yarnpkg/lockfile/1.1.0", write.getId().toString());
		assertEquals("clearlydefined", write.getAuthority());
		assertEquals("BSD-2-Clause", write.getLicense());
		assertEquals("https://clearlydefined.io/definitions/npm/npmjs/@yarnpkg/lockfile/1.1.0", write.getUrl());
		assertEquals(53, write.getScore());
		assertEquals(LicenseSupport.Status.Restricted, write.getStatus());
	}

	@Test
	void testEmptyRequest() {
		List<IContentData> results = new ArrayList<>();
		new TestContext().getClearlyDefinedService().queryLicenseData(Collections.emptySet(),
				data -> results.add(data));

		assertEquals(0, results.size());
	}

	@Test
	void testWithUnsupported() {
		List<IContentData> results = new ArrayList<>();
		new TestContext().getClearlyDefinedService().queryLicenseData(
				Collections.singleton(ContentId.getContentId("p2/eclipseplugin/-/write/0.2.0")),
				data -> results.add(data));

		assertEquals(0, results.size());
	}

	@Nested
	class TestServiceMethods {

		ClearlyDefinedSupport getClearlyDefinedService() {
			return (ClearlyDefinedSupport) new TestContext().getClearlyDefinedService();
		}

		@Test
		void testAcceptable() {
		// @formatter:off
			String json =
					"{\n" +
							"    \"licensed\": {\n" +
							"        \"declared\": \"EPL-2.0\", \n" +
							"        \"facets\": {\n" +
							"            \"core\": {\n" +
							"                \"discovered\": {\n" +
							"                    \"expressions\": [\n" +
							"                        \"EPL-2.0\",\n" +
							"                        \"NONE\"\n" +
							"                    ] \n" +
							"                }, \n" +
							"                \"files\": 4\n" +
							"            }\n" +
							"        }, \n" +
							"        \"score\": {\n" +
							"            \"total\": 80\n" +
							"        }\n" +
							"    }\n" +
							"}\n";
			// @formatter:on

			ClearlyDefinedContentData data = new ClearlyDefinedContentData("id",
					JsonUtils.readJson(new StringReader(json)));

			assertTrue(getClearlyDefinedService().isAccepted(data));

		}

		@Test
		void testUnacceptableByScore() {
		// @formatter:off
			String json =
					"{\n" +
							"    \"licensed\": {\n" +
							"        \"declared\": \"MIT\", \n" +
							"        \"facets\": {\n" +
							"            \"core\": {\n" +
							"                \"discovered\": {\n" +
							"                    \"expressions\": [\n" +
							"                        \"MIT\"\n" +
							"                    ] \n" +
							"                }, \n" +
							"                \"files\": 4\n" +
							"            }\n" +
							"        }, \n" +
							"        \"score\": {\n" +
							"            \"total\": 50\n" +
							"        }\n" +
							"    }\n" +
							"}\n";
			// @formatter:on

			ClearlyDefinedContentData data = new ClearlyDefinedContentData("id",
					JsonUtils.readJson(new StringReader(json)));

			assertFalse(getClearlyDefinedService().isAccepted(data));

		}

		@Test
		void testUnacceptableByDiscoveredLicense() {
		// @formatter:off
			String json =
					"{\n" +
							"    \"licensed\": {\n" +
							"        \"declared\": \"EPL-2.0\", \n" +
							"        \"facets\": {\n" +
							"            \"core\": {\n" +
							"                \"discovered\": {\n" +
							"                    \"expressions\": [\n" +
							"                        \"MIT\",\n" +
							"                        \"GPL-2.0\"\n" +
							"                    ] \n" +
							"                }, \n" +
							"                \"files\": 4\n" +
							"            }\n" +
							"        }, \n" +
							"        \"score\": {\n" +
							"            \"total\": 80\n" +
							"        }\n" +
							"    }\n" +
							"}\n";
			// @formatter:on

			ClearlyDefinedContentData data = new ClearlyDefinedContentData("id",
					JsonUtils.readJson(new StringReader(json)));

			assertFalse(getClearlyDefinedService().isAccepted(data));
		}
	}
}
