package io.f104a.mirror.repositories;

import io.f104a.mirror.Constants;
import io.f104a.mirror.repositories.annotations.Tool;
import io.f104a.mirror.tools.ITool;

/**
 * In this class all repositories for
 * statically instantiated objects are stored.
 * Each repository should be added here. The
 * type should be IRepository to avoid issues
 * if then repository type changes for some reason
 */
public final class Repositories {
    IRepository<Class<ITool>> TOOLS_REPOSITORY = new AnnotatedRepository<Tool, ITool>(Tool.class,
            Constants.PACKAGE_NAME);
}
