import model.Game;
import model.World;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: glotovd
 * Date: 13.11.13
 * Time: 18:47
 * To change this template use File | Settings | File Templates.
 */
public class GUIFrame extends JFrame {
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final String NAME = "GUI TEST";
    private final int screenWidth;
    public final DrawPanel panel;
    public final World world;
    public final Game game;

    public GUIFrame(World world, Game game, MyStrategy myStrategy) throws HeadlessException {
        this.world = world;
        this.game = game;
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        setSize(world.getWidth()*33, world.getHeight()*33);
        setTitle(NAME);
        setResizable(false);
        setLocation(1000,0);

        panel = new DrawPanel(this);
        panel.setFocusable(true);
        panel.setBackground(Color.LIGHT_GRAY);
        add(panel);

    }


    public void updateGraphics() {
        panel.paintComponent(getGraphics());

    }
}
