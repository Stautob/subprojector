package ch.stautob.eclipse.subprojector.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.stautob.eclipse.subprojector.infrastructure.ProjectUtil;


/**
 * Our sample handler extends AbstractHandler, an IHandler base class
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenCloseRecursiveHandler extends AbstractHandler {

   /**
    * {@inheritDoc}
    */
   @Override
   public Object execute(final ExecutionEvent event) throws ExecutionException {

      ISelection selection = HandlerUtil.getCurrentSelection(event);
      Collection<IProject> selectedProjects = updateSelection(selection);
      String action = event.getParameter("ch.stautob.eclipse.subprojector.commands.openCloseRecorsive.param");
      switch (action) {
      case "open":
         new Job("Open Project Recursively") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
               executeActionOnProjectRecursive(selectedProjects, monitor, (p, m) -> {
                  try {
                     p.open(m);
                  } catch (CoreException e1) {
                     e1.printStackTrace();
                  }
               });
               return Status.OK_STATUS;
            }

         }.schedule();
         break;
      case "close":
         new Job("Close Project Recursively") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
               executeActionOnProjectRecursive(selectedProjects, monitor, (p, m) -> {
                  try {
                     p.close(m);
                  } catch (CoreException e) {
                     e.printStackTrace();
                  }
               });
               return Status.OK_STATUS;
            }

         }.schedule();

         break;
      }
      return null;
   }

   protected Collection<IProject> updateSelection(ISelection selection) {
      if (!(selection instanceof IStructuredSelection)) return Collections.emptyList();
      Collection<IProject> selectedProjects = new ArrayList<>();
      for (Object selected : ((IStructuredSelection) selection).toList()) {
         if (selected instanceof IProject) {
            selectedProjects.add((IProject) selected);
         } else if (selected instanceof IAdaptable) {
            IProject proj = ((IAdaptable) selected).getAdapter(IProject.class);
            if (proj != null) selectedProjects.add(proj);
         }
      }
      return selectedProjects;
   }

   private void executeActionOnProjectRecursive(Collection<IProject> selectedProjects, IProgressMonitor monitor,
         BiConsumer<IProject, IProgressMonitor> action) {
      Set<IProject> allNestedProjects = selectedProjects.stream().flatMap(p -> ProjectUtil.getNestedProjects(p).stream()).collect(Collectors.toSet());
      SubMonitor outerSubmonitor = SubMonitor.convert(monitor, selectedProjects.size() + allNestedProjects.size());
      outerSubmonitor.worked(selectedProjects.size());
      allNestedProjects.forEach(p -> action.accept(p, outerSubmonitor.newChild(1)));
   }

}
