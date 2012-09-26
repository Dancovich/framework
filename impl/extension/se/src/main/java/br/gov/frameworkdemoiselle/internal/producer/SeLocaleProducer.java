package br.gov.frameworkdemoiselle.internal.producer;

import java.util.Locale;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

public class SeLocaleProducer {

	@Produces
	@Default
	public Locale create() {
		return Locale.getDefault();
	}

}
