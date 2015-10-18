package gyurix.animation;

/**
 * Created by GyuriX on 2015.05.21..
 */
public interface CustomEffect{
    String next(String in);
    String getText(String in);
    void setText(String newText);
    CustomEffect clone();
}
