class Special {
    protected final String field = "Super field";

    public static void main(String[] args) {
        Special s = new Special();
        s.f();
        Sub sub = new Sub();
        sub.f();
    }

    void f() {
        this.g();
    }

    void g() {
        System.out.println("Hello!");
        System.out.println(field);
    }
}

class Sub extends Special {
    protected final String field = "Sub field";

    void g() {
        System.out.println("Bonjour!");
        System.out.println(field);
        super.g();
        System.out.println(super.field);
        System.out.println("Bonjour!");
    }
}
