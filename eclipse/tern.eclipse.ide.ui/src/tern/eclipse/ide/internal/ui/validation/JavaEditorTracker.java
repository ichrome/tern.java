/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 *     Angelo Zerr <angelo.zerr@gmail.com> - adapt https://github.com/jbosstools/jbosstools-base/blob/master/common/plugins/org.jboss.tools.common.validation/src/org/jboss/tools/common/validation/java/JavaEditorTracker.java for Tern.
 ******************************************************************************/
package tern.eclipse.ide.internal.ui.validation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import tern.eclipse.ide.core.IDETernProject;
import tern.eclipse.ide.core.utils.FileUtils;
import tern.eclipse.ide.internal.ui.Trace;
import tern.eclipse.ide.ui.TernUIPlugin;
import tern.eclipse.ide.ui.utils.EditorUtils;

/**
 * Javascript editor tracker.
 */
public class JavaEditorTracker implements IWindowListener, IPageListener,
		IPartListener {
	static JavaEditorTracker INSTANCE;

	Map<IEditorPart, JavaDirtyRegionProcessor> fAsYouTypeValidators = new HashMap<IEditorPart, JavaDirtyRegionProcessor>();

	private JavaEditorTracker() {
		init();
	}

	public static JavaEditorTracker getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JavaEditorTracker();
		}
		return INSTANCE;
	}

	private void init() {
		if (PlatformUI.isWorkbenchRunning()) {
			IWorkbench workbench = TernUIPlugin.getDefault().getWorkbench();
			if (workbench != null) {
				IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
				for (IWorkbenchWindow window : windows) {
					windowOpened(window);
				}
				TernUIPlugin.getDefault().getWorkbench()
						.addWindowListener(this);
			}
		}
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		IWorkbenchPage[] pages = window.getPages();
		for (IWorkbenchPage page : pages) {
			pageClosed(page);
		}
		window.removePageListener(this);
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		if (window.getShell() != null) {
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page : pages) {
				pageOpened(page);
			}
			window.addPageListener(this);
		}
	}

	@Override
	public void pageActivated(IWorkbenchPage page) {
	}

	@Override
	public void pageClosed(IWorkbenchPage page) {
		IEditorReference[] rs = page.getEditorReferences();
		for (IEditorReference r : rs) {
			IEditorPart part = r.getEditor(false);
			if (part != null) {
				editorClosed(part);
			}
		}
		page.removePartListener(this);
	}

	@Override
	public void pageOpened(IWorkbenchPage page) {
		IEditorReference[] rs = page.getEditorReferences();
		for (IEditorReference r : rs) {
			IEditorPart part = r.getEditor(false);
			if (part != null) {
				editorOpened(part);
			}
		}
		page.addPartListener(this);
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorClosed((IEditorPart) part);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorOpened((IEditorPart) part);
		}
	}

	private void editorOpened(IEditorPart part) {
		if (part instanceof ITextEditor) {
			IResource resource = EditorUtils.getResource(part);
			if (resource != null && resource.getType() == IResource.FILE
					&& FileUtils.isJSFile(resource)
					&& IDETernProject.hasTernNature(resource.getProject())) {
				ISourceViewer viewer = EditorUtils.getSourceViewer(part);
				if (viewer != null) {
					JavaDirtyRegionProcessor processor = fAsYouTypeValidators
							.get(part);
					if (processor != null) {
						// Emulate editor closed due to uninstall the old
						// processor
						editorClosed(part);
						Assert.isTrue(null == fAsYouTypeValidators.get(part),
								"An old JavaDirtyRegionProcessor is not un-installed on Java Editor instance");
					}

					try {
						processor = new JavaDirtyRegionProcessor(
								(ITextEditor) part, (IFile) resource);
						processor.install(viewer);
						processor.setDocument(viewer.getDocument());
						processor.startReconciling();
						fAsYouTypeValidators.put(part, processor);
					} catch (CoreException e) {
						Trace.trace(
								Trace.SEVERE,
								"Error while getting tern project for validation.",
								e);
					}
				}
			}
		}
	}

	private void editorClosed(IEditorPart part) {
		if (part instanceof ITextEditor) {
			JavaDirtyRegionProcessor processor = fAsYouTypeValidators
					.remove(part);
			if (processor != null) {
				processor.uninstall();
				Assert.isTrue(null == fAsYouTypeValidators.get(part),
						"An old JavaDirtyRegionProcessor is not un-installed on Java Editor instance");
			}
		}
	}
}