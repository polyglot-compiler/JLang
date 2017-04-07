package unit;

class Special {

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
    }
}

class Sub extends Special {
    void g() {
        System.out.println("Bonjour!");
        super.g();
        System.out.println("Bonjour!");
    }
}
