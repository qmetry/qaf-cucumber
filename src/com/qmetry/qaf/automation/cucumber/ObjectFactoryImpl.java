package com.qmetry.qaf.automation.cucumber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;

import com.qmetry.qaf.automation.step.DefaultObjectFactory;
import com.qmetry.qaf.automation.step.ObjectFactory;

import io.cucumber.core.runtime.ObjectFactorySupplier;

/**
 * Object factory implementation for QAF to use cucumber provided object factory
 * that does dependency injection. This class will be used when running your BDD with QAF
 * BDD Factory and you used cucumber object factory that does dependency injection.
 * 
 * @author chirag.jayswal
 *
 */
public class ObjectFactoryImpl implements ObjectFactory {
	private static final Log logger = LogFactoryImpl.getLog(ObjectFactoryImpl.class);

	private ObjectFactorySupplier supplier;

	public ObjectFactoryImpl(ObjectFactorySupplier supplier) {
		this.supplier = supplier;
	}

	@Override
	public <T> T getObject(Class<T> cls) throws Exception {
		io.cucumber.core.backend.ObjectFactory objectFactory = supplier.get();
		try {
			return objectFactory.getInstance(cls);
		} catch (Exception e) {
			//fall-back
			logger.debug("Unable to crete obect of class["+cls+"] using ["+objectFactory.getClass()+"]. Using default qaf obect factory as fallback");
			return new DefaultObjectFactory().getObject(cls);
		}
	}

	public void setFactory(ObjectFactorySupplier supplier) {
		this.supplier = supplier;
	}
}
