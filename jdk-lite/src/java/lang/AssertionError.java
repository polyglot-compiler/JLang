//Copyright (C) 2018 Cornell University

package java.lang;

public class AssertionError extends Error {

    public AssertionError() {
    }

    private AssertionError(String detailMessage) {
        super(detailMessage);
    }

    public AssertionError(Object detailMessage) {
        this(detailMessage.toString());
    }
}
