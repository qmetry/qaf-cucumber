/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import java.util.function.Supplier;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;

/**
 * @author chirag.jayswal
 *
 */
public class QAFBackendProviderService implements BackendProviderService {

	/* (non-Javadoc)
	 * @see io.cucumber.core.backend.BackendProviderService#create(io.cucumber.core.backend.Lookup, io.cucumber.core.backend.Container, java.util.function.Supplier)
	 */
	@Override
	public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader) {
		return new QAFBackend(lookup, container, classLoader);
	}

}
