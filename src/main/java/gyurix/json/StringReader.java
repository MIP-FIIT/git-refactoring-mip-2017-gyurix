package gyurix.json;

public class StringReader {
    public int id;
    public char[] str;

    public StringReader(String in) {
        this.str = in.toCharArray();
    }

    public boolean hasNext() {
        return this.id < this.str.length;
    }

    public char last() {
        return this.str[this.id - 1];
    }

    public char next() {
        return this.str[this.id++];
    }
}

