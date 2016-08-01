
public class IntLinkedList {

    private int data;
    private IntLinkedList next;

    public IntLinkedList next() {
        return next;
    }

    public int data() {
        return data;
    }

    public void append(int i) {
        IntLinkedList temp = this;
        while (temp.next != null) {
            temp = temp.next;
        }
        addSuccessor(i);
    }

    private void addSuccessor(int i) {
        next = new IntLinkedList();
        next.data = i;
    }

}
