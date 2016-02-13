package gyurix.json;

public class StringReader {
    public char[] str;
    public int id;

    public StringReader(String in) {
        this.str = in.toCharArray();
    }

    public char next() {
        return this.str[this.id++];
    }

    public boolean hasNext() {
        return this.id < this.str.length;
    }

    public char last() {
        return this.str[this.id - 1];
    }
}

