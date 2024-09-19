package descriptor;

public class CharInterval {

    char a;
    char b;

    public CharInterval(char a, char b) {
        if (a <= b) {
            this.a = a;
            this.b = b;
        } else {
            this.a = b;
            this.b = a;
        }

    }

    public CharInterval(int a, int b) {
        this((char) a, (char) b);
    }

    public boolean contains(char c) {
        return c >= this.a && c <= this.b;
    }

}
