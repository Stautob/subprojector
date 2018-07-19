package ch.stautob.eclipse.subprojector.infrastructure;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;


public class ProjectUtil {

   public static Set<IProject> getNestedProjectsExclusive(final IProject initialProject) {
      return getNestedProjects(initialProject).stream().filter(p -> p != initialProject).collect(Collectors.toSet());
   }

   public static Set<IProject> getNestedProjects(final IProject initialProject) {
      if (initialProject == null) { return Collections.emptySet(); }
      final IPath initialProjectPath = initialProject.getLocation();
      return Stream.of(initialProject.getWorkspace().getRoot().getProjects()).filter(p -> p.getLocation() != null).filter(p -> initialProjectPath
            .isPrefixOf(p.getLocation())).collect(Collectors.toSet());
   }
}
