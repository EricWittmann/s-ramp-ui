/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.ui.client.services.i18n;

import java.util.Date;
import java.util.Map;

import org.overlord.sramp.ui.client.services.AbstractService;
import org.overlord.sramp.ui.client.services.IServiceLifecycleListener;
import org.overlord.sramp.ui.client.services.ServiceLifecycleContext;
import org.overlord.sramp.ui.shared.rsvcs.ILocalizationRemoteService;
import org.overlord.sramp.ui.shared.rsvcs.ILocalizationRemoteServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A concrete implementation of an {@link ILocalizationService}.
 *
 * @author eric.wittmann@redhat.com
 */
public class LocalizationService extends AbstractService implements ILocalizationService {

	private final ILocalizationRemoteServiceAsync localizationRemoteService = GWT.create(ILocalizationRemoteService.class);

	private LocalizationDictionary messages;
	private DateTimeFormat dateFormat;
	private DateTimeFormat dateTimeFormat;
	
	/**
	 * Constructor.
	 */
	public LocalizationService() {
	}

	/**
	 * @see org.overlord.sramp.ui.client.services.AbstractService#start(org.overlord.sramp.ui.client.services.ServiceLifecycleContext, org.overlord.sramp.ui.client.services.IServiceLifecycleListener)
	 */
	@Override
	public void start(ServiceLifecycleContext context, final IServiceLifecycleListener serviceListener) {
		localizationRemoteService.getMessages(new AsyncCallback<Map<String, String>>() {
			@Override
			public void onSuccess(Map<String, String> result) {
				initDictionary(result);
				serviceListener.onStarted();
			}
			@Override
			public void onFailure(Throwable caught) {
				serviceListener.onError(caught);
			}
		});
	}

	/**
	 * Initializes the localization dictionary.
	 * @param messages
	 */
	protected void initDictionary(Map<String, String> messages) {
		this.messages = LocalizationDictionary.create(messages);
		String dFormat = this.messages.get("formats.date");
		String dtFormat = this.messages.get("formats.datetime");
		if (dFormat == null)
			dFormat = "yyyy-MM-dd";
		if (dtFormat == null)
			dtFormat = "yyyy-MM-dd HH.mm.ss vvv";
		this.dateFormat = DateTimeFormat.getFormat(dFormat);
		this.dateTimeFormat = DateTimeFormat.getFormat(dtFormat);
	}

	/**
	 * @see org.overlord.sramp.ui.client.services.i18n.ILocalizationService#translate(java.lang.String, java.lang.Object[])
	 */
	@Override
	public String translate(String key, Object ... args) {
		String val = this.messages.get(key);
		if (val == null)
			return "**" + key + "**";
		if (args == null || args.length == 0)
			return val;

		// Format the string and replace arguments with the supplied values.
		// Note: GWT can do this with their static i18n approach, but I couldn't find anything obvious 
		// for dynamic formatting - though there probably is something I should be using 
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(val);
			int argStartIdx = builder.indexOf("{");
			while (argStartIdx != -1) {
				int argEndIdx = builder.indexOf("}");
				String argSpec = builder.substring(argStartIdx + 1, argEndIdx);
				String [] split = argSpec.split(",");
				String argNumStr = split[0];
				int argNum = Integer.parseInt(argNumStr);
				Object argValue = args[argNum];
				String argType = "string";
				String argFormat = null;
				String formattedArgValue = null;
				if (split.length > 1)
					argType = split[1];
				if (split.length > 2)
					argFormat = split[2];
				if ("string".equals(argType)) {
					formattedArgValue = String.valueOf(argValue);
				} else if ("date".equals(argType)) {
					formattedArgValue = this.dateFormat.format((Date) argValue);
				} else if ("datetime".equals(argType)) {
					formattedArgValue = this.dateTimeFormat.format((Date) argValue);
				} else {
					// TODO create a formatter of the right type and use it!
					formattedArgValue = "TBD:"+argFormat;
				}
				builder.replace(argStartIdx, argEndIdx+1, formattedArgValue);
				argStartIdx = builder.indexOf("{");
			}
			return builder.toString();
		} catch (Throwable t) {
			return "!!" + key + "!!";
		}
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.services.i18n.ILocalizationService#formatDate(java.util.Date)
	 */
	@Override
	public String formatDate(Date date) {
		return this.dateFormat.format(date);
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.services.i18n.ILocalizationService#formatDateTime(java.util.Date)
	 */
	@Override
	public String formatDateTime(Date dateTime) {
		return this.dateTimeFormat.format(dateTime);
	}
	
}
