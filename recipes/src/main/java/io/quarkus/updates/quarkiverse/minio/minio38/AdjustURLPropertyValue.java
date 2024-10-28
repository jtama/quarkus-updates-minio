package io.quarkus.updates.quarkiverse.minio.minio38;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.service.ImportService;
import org.openrewrite.java.tree.J;

@Value
@EqualsAndHashCode(callSuper = true)
public class AdjustURLPropertyValue extends Recipe {

    @Override
    public String getDisplayName() {
        return "Adust quarkus.minio.url property key";
    }

    @Override
    public String getDescription() {
        return "Adjust quarkus.minio.url property key to quarkus.minio.host";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new Preconditions.Check(new UsesType<>("org.eclipse.microprofile.config.inject.ConfigProperty", true), new JavaIsoVisitor<>() {
            private final AnnotationMatcher configPropertyMatcher = new AnnotationMatcher("@org.eclipse.microprofile.config.inject.ConfigProperty");
            private final JavaTemplate configPropertyAfter = JavaTemplate.builder("@org.eclipse.microprofile.config.inject.ConfigProperty(name = \"quarkus.minio.host\")")
                    .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                    .build();

            @Override
            public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
                J.Annotation visited = super.visitAnnotation(annotation, ctx);
                if (!configPropertyMatcher.matches(visited)) {
                    return visited;
                }
                visited = configPropertyAfter.apply(getCursor(), annotation.getCoordinates().replace());
                doAfterVisit(service(ImportService.class).shortenFullyQualifiedTypeReferencesIn(visited));
                return visited;
            }
        });
    }
}
