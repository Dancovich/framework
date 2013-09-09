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
/*
 * Demoiselle Framework Copyright (c) 2010 Serpro and other contributors as indicated by the @author tag. See the
 * copyright.txt in the distribution for a full listing of contributors. Demoiselle Framework is an open source Java EE
 * library designed to accelerate the development of transactional database Web applications. Demoiselle Framework is
 * released under the terms of the LGPL license 3 http://www.gnu.org/licenses/lgpl.html LGPL License 3 This file is part
 * of Demoiselle Framework. Demoiselle Framework is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License 3 as published by the Free Software Foundation. Demoiselle Framework
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You
 * should have received a copy of the GNU Lesser General Public License along with Demoiselle Framework. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package br.gov.frameworkdemoiselle.internal.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.context.ConversationContext;
import br.gov.frameworkdemoiselle.context.CustomContext;
import br.gov.frameworkdemoiselle.context.RequestContext;
import br.gov.frameworkdemoiselle.context.SessionContext;
import br.gov.frameworkdemoiselle.context.StaticContext;
import br.gov.frameworkdemoiselle.context.ViewContext;
import br.gov.frameworkdemoiselle.internal.bootstrap.CustomContextBootstrap;
import br.gov.frameworkdemoiselle.internal.implementation.StrategySelector;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Produces instances of {@link CustomContext} to control contexts not activated
 * by the container
 * 
 * @author serpro
 *
 */
@ApplicationScoped
public class CustomContextProducer {
	
	private Logger logger;

	private transient ResourceBundle bundle;
	
	private List<CustomContext> contexts = new ArrayList<CustomContext>();
	
	
	/**
	 * Store a context into this producer. The context must have
	 * been registered into CDI by a portable extension, this method will not do this.
	 *  
	 */
	public void addRegisteredContext(CustomContext context){
		Logger logger = getLogger();
		ResourceBundle bundle = getBundle();
		
		if (!contexts.contains(context)){
			contexts.add(context);
			logger.debug( bundle.getString("bootstrap-context-added", context.getClass().getCanonicalName() , context.getScope().getSimpleName() ) );
		}
		else{
			logger.debug( bundle.getString("bootstrap-context-already-managed", context.getClass().getCanonicalName() , context.getScope().getSimpleName() ) );
		}
	}

	/**
	 * Store a list of contexts into this producer. The contexts must have
	 * been registered into CDI by a portable extension, this method will not do this.
	 *  
	 */
	@PostConstruct
	public void addRegisteredContexts(){
		CustomContextBootstrap contextBootstrap = Beans.getReference(CustomContextBootstrap.class);
		
		List<CustomContext> contexts = contextBootstrap.getCustomContexts();
		
		for (CustomContext context : contexts){
			addRegisteredContext(context);
		}
	}
	
	/**
	 * Deactivates all registered contexts and clear the context collection
	 */
	@PreDestroy
	public void closeContexts(){
		//Desativa todos os contextos registrados.
		for (CustomContext context : contexts){
			context.deactivate();
		}
		
		contexts.clear();
	}
	
	/////////////PRODUCERS///////////////////
	
	@Produces
	public RequestContext getRequestContext(InjectionPoint ip , CustomContextBootstrap extension){
		return getContext(ip, extension);
	}
	
	@Produces
	public SessionContext getSessionContext(InjectionPoint ip , CustomContextBootstrap extension){
		return getContext(ip, extension);
	}
	
	@Produces
	public ViewContext getViewContext(InjectionPoint ip , CustomContextBootstrap extension){
		return getContext(ip, extension);
	}
	
	@Produces
	public StaticContext getStaticContext(InjectionPoint ip , CustomContextBootstrap extension){
		return getContext(ip, extension);
	}
	
	@Produces
	public ConversationContext getConversationContext(InjectionPoint ip , CustomContextBootstrap extension){
		return getContext(ip, extension);
	}
	
	/////////////END OF PRODUCERS///////////////////
	
	@SuppressWarnings("unchecked")
	private <T extends CustomContext> T getContext(InjectionPoint ip , CustomContextBootstrap extension){
		T producedContext = null;
		
		if (ip!=null){
			Class<T> beanClass = (Class<T>) ip.getType();
			producedContext = (T) getContext(beanClass);
		}
		
		if (producedContext!=null){
			getLogger().debug( getBundle().getString("custom-context-selected" , producedContext.getClass().getCanonicalName()) );
		}
		
		return producedContext;
	}
	
	private CustomContext getContext(Class<? extends CustomContext> contextClass){
		CustomContext producedContext = null;
		
		ArrayList<CustomContext> selectableContexts = new ArrayList<CustomContext>();
		
		for (CustomContext context : contexts){
			if ( contextClass.isAssignableFrom( context.getClass() ) ){
				if (context.isActive()){
					producedContext = context;
					break;
				}
				else{
					selectableContexts.add(context);
				}
			}
		}
		
		if (producedContext==null && !selectableContexts.isEmpty()){
			producedContext = StrategySelector.selectInstance(CustomContext.class, selectableContexts);
		}
		
		return producedContext;
	}
	
	private Logger getLogger() {
		if (this.logger == null) {
			this.logger = LoggerProducer.create(this.getClass());
		}

		return this.logger;
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = new ResourceBundle("demoiselle-core-bundle", Locale.getDefault());
		}

		return bundle;
	}

}
