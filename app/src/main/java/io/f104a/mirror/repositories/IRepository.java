package io.f104a.mirror.repositories;

import java.util.List;

/**
 * Repository interface. All repositories storing instances
 * should implement this interface.
 *
 * @param <T> Type which instances stored in repository
 */
public interface IRepository<T> {
    List<T> getAllRegistered();
}
