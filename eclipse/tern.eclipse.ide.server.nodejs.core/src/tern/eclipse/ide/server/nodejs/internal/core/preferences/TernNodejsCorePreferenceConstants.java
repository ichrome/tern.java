/*******************************************************************************
 * Copyright (c) 2013 Angelo ZERR.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:      
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package tern.eclipse.ide.server.nodejs.internal.core.preferences;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import tern.eclipse.ide.server.nodejs.core.TernNodejsCoreConstants;
import tern.eclipse.ide.server.nodejs.core.TernNodejsCorePlugin;
import tern.eclipse.ide.server.nodejs.internal.core.NodejsInstall;

public class TernNodejsCorePreferenceConstants {

	/**
	 * Initializes the given preference store with the default values.
	 * 
	 * @param store
	 *            the preference store to be initialized
	 */
	public static void initializeDefaultValues() {
		IEclipsePreferences node = new DefaultScope()
				.getNode(TernNodejsCorePlugin.PLUGIN_ID);
		// By default native node.js install is used.
		node.put(TernNodejsCoreConstants.NODEJS_INSTALL,
				NodejsInstall.NODE_NATIVE);
	}

	// Don't instantiate
	private TernNodejsCorePreferenceConstants() {
	}
}
