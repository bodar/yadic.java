package com.googlecode.yadic;

import java.util.ArrayList;
import java.util.List;

public class ContainerException extends RuntimeException{
    private List<ContainerException> causes;

    public ContainerException(String message, List<ContainerException> causes) {
        super(message, lastOrNull(causes));
        this.causes = causes;
    }

    private static ContainerException lastOrNull(List<ContainerException> causes) {
        if(causes.isEmpty()) return null;
        return causes.get(causes.size() - 1);
    }

    public ContainerException(String message) {
      this(message, new ArrayList<ContainerException>());
    }

    public List<ContainerException> getCauses() {
        return causes;
    }
}