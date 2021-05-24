package rabbitminer.UI.Tools;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

/**
 *
 * @author Nikos Siatras
 */
public class UIHelper
{

    static
    {

    }

    /**
     * Move the form in the center of the screen
     *
     * @param frm is the form to move
     */
    public static void MoveFormInCenterOfScreen(JFrame frm)
    {
        // Open form in middle of Screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = frm.getSize().width;
        int h = frm.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        frm.setLocation(x, y);
    }

}
