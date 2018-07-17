package ch.stautob.eclipse.subprojector.handlers;

import org.eclipse.core.resources.IProject;

import ch.stautob.eclipse.subprojector.infrastructure.ProjectUtil;


public class TestAllOpenAllClosedRecursive extends org.eclipse.core.expressions.PropertyTester {

   @Override
   public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
      switch (property) {
      case "allOpen":
         return testAllOpen((IProject) receiver) == ((Boolean) expectedValue).booleanValue();
      case "allClosed":
         return testAllClosed((IProject) receiver) == ((Boolean) expectedValue).booleanValue();
      }
      return false;
   }

   private boolean testAllClosed(IProject project) {
      if (!project.isOpen()) return ProjectUtil.getNestedProjects(project).stream().noneMatch(IProject::isOpen);
      return false;
   }

   private boolean testAllOpen(IProject project) {
      if (project.isOpen()) return ProjectUtil.getNestedProjects(project).stream().allMatch(IProject::isOpen);
      return false;
   }
}
