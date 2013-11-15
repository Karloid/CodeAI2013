import model.*;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: glotovd
 * Date: 13.11.13
 * Time: 18:52
 * To change this template use File | Settings | File Templates.
 */
public class DrawPanel extends JPanel {
    private static final int CELL_SIZE = 32;
    public static final int Y_CORRECT = 0;
    public World world;
    public Game game;
    private MyStrategy strategy;

    public DrawPanel(GUIFrame guiFrame) {
        //     updateContext(guiFrame.world, guiFrame.game);

    }

    public void updateContext(World world, Game game, MyStrategy myStrategy) {
        this.world = world;
        this.game = game;
        this.strategy = myStrategy;
    }

    public void drawTest(Graphics g) {
        paintComponent(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // drawBonus(g);
        drawCells(g);
     //   drawVisible(g);
      //  drawCanShoot(g);
        drawBonus(g);
        drawTrooper(g);
        drawWarning(g);
        //.setColor(Color.BLUE);
        //g.fillRect(15, 15, 100, 100);

    }

    private void drawWarning(Graphics g) {
        int[][] result =  new int [world.getWidth()][world.getHeight()];
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                for (Trooper trooper : world.getTroopers()) {
                    if (trooper.isTeammate() && world.isVisible(trooper.getShootingRange(), trooper.getX(), trooper.getY(), trooper.getStance(),
                                x, y, TrooperStance.STANDING) && world.getCells()[x][y] == CellType.FREE) {
                        g.setColor(Color.BLUE);
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE + Y_CORRECT, CELL_SIZE,CELL_SIZE);
                    }
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
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE + Y_CORRECT, CELL_SIZE,CELL_SIZE);
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
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE + Y_CORRECT, CELL_SIZE,CELL_SIZE);
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
