package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.converters.models.ConverterRequest;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ElementConverter<F, T, R extends ConverterRequest<F>> implements IElementConverter<F, T, R> {

    @Override
    public List<T> convertAll(Collection<R> from) {
        return from.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<F> reverseAll(Collection<T> to) {
        return to.stream().map(this::reverse).collect(Collectors.toList());
    }
}
