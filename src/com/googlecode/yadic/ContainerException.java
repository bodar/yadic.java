package com.googlecode.yadic;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ContainerException extends RuntimeException{
    private List<? extends Exception> causes;

    public ContainerException(String message, List<? extends Exception> causes) {
        super(message, lastOrNull(causes));
        this.causes = causes;
    }

    private static Exception lastOrNull(List<? extends Exception> causes) {
        if(causes.isEmpty()) return null;
        return causes.get(causes.size() - 1);
    }

    public ContainerException(String message) {
      this(message, new ArrayList<Exception>());
    }

    public ContainerException(String message, Exception e) {
      this(message, asList(e));
    }

    public List<? extends Exception> getCauses() {
        return causes;
    }
}