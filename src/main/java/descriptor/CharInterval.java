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

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CharInterval))
            return false;
        CharInterval o2 = (CharInterval) other;
        return o2.a == this.a && o2.b == this.b;
    }

    public CharInterval copy() {
        return new CharInterval(this.a, this.b);
    }

    @Override
    public String toString() {
        return "[" + (int) a + "," + (int) b + "]";
    }

    public boolean hasCommonChar(CharInterval other) {
        if (other.contains(b) || other.contains(a) || contains(other.b) || contains(other.a))
            return true;
        return false;
    }

    public Character getRandomExample() {
        int index = (int) (Math.random() * (b - a));

        return (char) (a + index);

    }
}
