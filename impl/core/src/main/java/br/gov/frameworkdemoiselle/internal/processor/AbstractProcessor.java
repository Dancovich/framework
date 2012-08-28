/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.internal.processor;

import java.util.Locale;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.BeanManager;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * It abstract the integration between Processor and the context;
 * 
 * @param <T>
 *            the declaring class
 */
public abstract class AbstractProcessor<T> implements Processor {

	private BeanManager beanManager;

	private AnnotatedCallable<T> annotatedCallable;

	private ResourceBundle bundle;

	protected static final String BUNDLE_BASE_NAME = "demoiselle-core-bundle";

	public AbstractProcessor(final BeanManager beanManager) {
		this.beanManager = beanManager;
	}

	public AbstractProcessor(final AnnotatedCallable<T> annotatedCallable, final BeanManager beanManager) {
		this.annotatedCallable = annotatedCallable;
		this.beanManager = beanManager;
	}

	protected AnnotatedCallable<T> getAnnotatedCallable() {
		return this.annotatedCallable;
	}

	protected BeanManager getBeanManager() {
		return this.beanManager;
	}

	/**
	 * Ask the bean manager for the firt instance of the declaring classe for this java member, then returns the current
	 * reference;
	 * 
	 * @param <T>
	 *            DeclaringClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected T getReferencedBean() {
		Class<T> classType = (Class<T>) getAnnotatedCallable().getJavaMember().getDeclaringClass();
		return Beans.getReference(classType);
	}

	protected ResourceBundle getBundle() {
		return getBundle(BUNDLE_BASE_NAME);
	}

	protected ResourceBundle getBundle(String baseName) {
		if (bundle == null) {
			bundle = ResourceBundleProducer.create(baseName, Locale.getDefault());
		}

		return bundle;
	}

	protected Logger getLogger() {
		return LoggerProducer.create(this.getClass());
	}
}
