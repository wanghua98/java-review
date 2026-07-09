package com.uniplore.foctory;

import com.uniplore.designpatterns.factory.ui.MacUIFactory;
import com.uniplore.designpatterns.factory.ui.UIFactory;
import com.uniplore.designpatterns.factory.ui.WindowsUIFactory;
import org.junit.Test;

/**
 * UI工厂测试类
 *
 * @author 杨锋
 */
public class UIFactoryTest {

    /**
     * 测试UI工厂类
     */
    @Test
    public void testUIFactory() {
        /*
          创建Mac风格的UI工厂对象
         */

        UIFactory uiFactory = new MacUIFactory();
        uiFactory.createButton().render();
        uiFactory.createTextField().render();

        /*
          创建Windows风格的UI工厂对象
         */
        uiFactory = new WindowsUIFactory();
        uiFactory.createButton().render();
        uiFactory.createTextField().render();

    }

}
