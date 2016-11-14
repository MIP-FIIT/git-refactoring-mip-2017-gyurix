package gyurix.configfile;

import java.lang.reflect.Type;

/**
 * Created by GyuriX on 2016. 08. 04..
 */
public interface ValueClassSelector {
    Class getValueClass();

    Type[] getValueTypes();
}
