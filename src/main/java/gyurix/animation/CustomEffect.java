package gyurix.animation;

public interface CustomEffect {
    String next(String var1);

    String getText();

    void setText(String var1);

    CustomEffect clone();
}

