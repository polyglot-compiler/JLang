class CastNull {

    static class InnerOne {

    }

    static interface InnerInterOne {
	public Object objectify();
    }

    static class InnerTwo implements InnerInterOne {
	public Object objectify() {
	    return this;
	}


    }

    static class InnerThree implements InnerInterOne {
	public Object objectify() {
	    return null;
	}
    }

    public static void main(String[] args) {

	InnerTwo t = null;
	System.out.println(t == null);
	t = new InnerTwo();
	InnerInterOne in = (InnerInterOne)t.objectify();
	System.out.println(in == null);
	in = (InnerInterOne) new InnerThree().objectify();
	System.out.println(in == null);
	InnerOne ino = (InnerOne) new InnerThree().objectify();
	System.out.println(ino == null);
	try {
	    Object o = new InnerTwo();
	    InnerOne o1 = (InnerOne) new InnerTwo().objectify();
	} catch (ClassCastException e) {
	    System.out.println("caught exception");
	}
    }

}