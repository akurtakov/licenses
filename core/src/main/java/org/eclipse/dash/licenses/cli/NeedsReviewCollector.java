/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport.Status;

/**
 * The "Needs Review" collector tracks the results that likely require some
 * review from the IP Team. The current implementation gathers results and dumps
 * output to an {@link OutputStream} when the instance is closed.
 */
public class NeedsReviewCollector implements IResultsCollector {

	private PrintWriter output;
	private List<LicenseData> needsReview = new ArrayList<>();

	public NeedsReviewCollector(OutputStream out) {
		output = new PrintWriter(out, false, StandardCharsets.UTF_8);
	}

	@Override
	public void accept(LicenseData data) {
		if (data.getStatus() != Status.Approved) {
			needsReview.add(data);
		}
	}

	@Override
	public void close() {
		if (needsReview.isEmpty()) {
			output.println(
					"Vetted license information was found for all content. No further investigation is required.");
		} else {
			output.println("License information could not be automatically verified for the following content:");
			output.println();
			needsReview.stream().map(LicenseData::getId).sorted().forEach(output::println);
			output.println();
			output.println("This content is either not correctly mapped by the system, or requires review.");

			output.println();
			output.println("");
		}
		output.flush();
	}

	@Override
	public int getStatus() {
		return needsReview.size();
	}
}
