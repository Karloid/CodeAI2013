import model.*;

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
    private static final String NAME = "Debug";
    private static final int CELL_SIZE = 32;
    private static final int Y_CORRECT = 0;
    private final int screenWidth;
    public World world;
    public Game game;
    private MyStrategy strategy;

    public GUIFrame(World world, Game game, MyStrategy myStrategy) throws HeadlessException {
        this.world = world;
        this.game = game;
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        screenWidth = screenSize.width;


        setSize(world.getWidth() * 33, world.getHeight() * 33);
        setTitle(NAME);
        setResizable(false);
        setLocation(1000, 0);
              /*
        panel = new DrawPanel(this);
        panel.setFocusable(true);
        panel.setBackground(Color.LIGHT_GRAY);
        add(panel);    */


    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        // drawBonus(g);
        drawCells(g);
        //   drawVisible(g);
        //  drawCanShoot(g);
        drawBonus(g);
        //   drawWarning(g);
        drawTrooper(g);
        drawPatch(g);

        drawSelf(g);

    }

    private void drawSelf(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(strategy.self.getX() * CELL_SIZE, strategy.self.getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    private void drawPatch(Graphics g) {
        g.setColor(Color.GRAY);
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                if (!strategy.available[x][y]) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
                g.setColor(Color.GRAY);
                g.drawString(strategy.path[x][y] + " ", x * CELL_SIZE + CELL_SIZE / 3, y * CELL_SIZE + CELL_SIZE / 2);
            }
            //     System.out.println();
        }

    }

    public void updateGraphics(World world, Game game, MyStrategy myStrategy) {
        this.world = world;
        this.game = game;
        this.strategy = myStrategy;
        repaint();
    }

    private void drawWarning(Graphics g) {
        int[][] result = new int[world.getWidth()][world.getHeight()];
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                for (Trooper trooper : world.getTroopers()) {
                    if (!trooper.isTeammate() && world.isVisible(trooper.getVisionRange(), trooper.getX(), trooper.getY(), trooper.getStance(),
                            x, y, TrooperStance.STANDING) && world.getCells()[x][y] == CellType.FREE) {
                        result[x][y] += 1;
                    }
                }
            }
        }

        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                if (result[x][y] > 0) {
                    g.setColor(Color.RED);
                    g.fillRect(x * CELL_SIZE + CELL_SIZE / 3, y * CELL_SIZE + CELL_SIZE / 3, CELL_SIZE - CELL_SIZE / 2, CELL_SIZE - CELL_SIZE / 2);
                    g.setColor(Color.BLACK);
                    g.drawString(String.valueOf(result[x][y]), x * CELL_SIZE + CELL_SIZE / 2, y * CELL_SIZE + CELL_SIZE / 2 + 6);
                }
            }
        }

    }

    private void drawVisible(Graphics g) {
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                for (Trooper trooper : world.getTroopers()) {
                    if (trooper.isTeammate() && world.isVisible(trooper.getVisionRange(), trooper.getX(), trooper.getY(), trooper.getStance(), x, y, TrooperStance.STANDING) && world.getCells()[x][y] == CellType.FREE) {
                        g.setColor(Color.BLUE
                        );
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE + Y_CORRECT, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }

    }

    private void drawCanShoot(Graphics g) {
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                for (Trooper trooper : world.getTroopers()) {
                    if (trooper.isTeammate() && world.isVisible(trooper.getShootingRange(), trooper.getX(), trooper.getY(), trooper.getStance(), x, y, TrooperStance.STANDING) && world.getCells()[x][y] == CellType.FREE) {
                        g.setColor(Color.GREEN);
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE + Y_CORRECT, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }
    }

    private void drawTrooper(Graphics g) {
        for (Trooper trooper : world.getTroopers()) {
            g.setColor(Color.BLACK);

            int x = trooper.getX() * CELL_SIZE;
            int y = trooper.getY() * CELL_SIZE;
            g.drawRect(x, y + Y_CORRECT, CELL_SIZE, CELL_SIZE);
            g.drawOval(x, y + Y_CORRECT, CELL_SIZE, CELL_SIZE);
            if (trooper.isTeammate()) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.RED);
            }
            if (trooper.getId() == strategy.self.getId()) {
                g.setColor(Color.BLUE);
            }
            g.fillOval(x, y + Y_CORRECT, CELL_SIZE, CELL_SIZE);
        }

    }

    private void drawBonus(Graphics g) {
        for (Bonus bonus : world.getBonuses()) {
            g.setColor(Color.BLACK);
            int x = bonus.getX() * CELL_SIZE;
            int y = bonus.getY() * CELL_SIZE;
            g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            // g.drawOval(x, y, CELL_SIZE, CELL_SIZE);

            if (bonus.getType() == BonusType.MEDIKIT) {
                g.drawLine(x, y + Math.round(CELL_SIZE / 2), x + CELL_SIZE, y + Math.round(CELL_SIZE / 2));
                g.drawLine(x + Math.round(CELL_SIZE / 2), y, x + Math.round(CELL_SIZE / 2), y + CELL_SIZE);
            }
        }

    }

    private void drawCells(Graphics g) {
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                if (world.getCells()[x][y] == CellType.FREE) {
                    g.setColor(Color.WHITE);
                } else if (world.getCells()[x][y] == CellType.HIGH_COVER) {
                    g.setColor(Color.BLACK);
                } else if (world.getCells()[x][y] == CellType.MEDIUM_COVER) {
                    g.setColor(Color.DARK_GRAY);
                } else if (world.getCells()[x][y] == CellType.LOW_COVER) {
                    g.setColor(Color.GRAY);
                }
                g.fillRect(x * CELL_SIZE, y * CELL_SIZE + Y_CORRECT, x * CELL_SIZE + CELL_SIZE, y * CELL_SIZE + CELL_SIZE + Y_CORRECT);
            }
        }
    }
}
