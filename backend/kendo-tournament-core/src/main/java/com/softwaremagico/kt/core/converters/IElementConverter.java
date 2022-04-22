package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.converters.models.ConverterRequest;

import java.util.Collection;

public interface IElementConverter<F, T, R extends ConverterRequest<F>> {
    T convert(R from);

    Collection<T> convertAll(Collection<R> from);

    F reverse(T to);

    Collection<F> reverseAll(Collection<T> to);
}
