import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static model.Direction.*;

public final class MyStrategy implements Strategy {
    private static final int MIN_HP_TO_THROW_GRENADE = 20;
    private static final int HP_TO_HEAL = 90;
    public static final int DAMAGE_TERPIM = 30;
    public static final int MAX_DISTANCE = 4;
    public static final int DISMIS_LENGTH_DISTANCE = 2;
    private final Random random = new Random();
    private Trooper self;
    private World world;
    private Game game;
    private Move move;
    private Trooper[] troopers;

    private static List<Point> movePoints;
    private static int movePointIndex = 0;

    private static boolean firsTimeRun = true;
    private static long capitanId;
    private Bonus[] bonuses;
    private static Long targetId;
    private static Trooper target;
    private static GUIFrame guiFrame;
    private List<Point> savedPositions;
    private static int dismissMoveIndex = -10;


    @Override
    public void move(Trooper self, World world, Game game, Move move) {

        init(self, world, game, move);

        initTarget();

        log("troopers size: " + troopers.length + " targetId: " + targetId + (target != null ? " target type: " + target.getType() : ""));
        if (firsTimeRun) {
            firstTimeInit();
            firsTimeRun = false;
            createGUI();
        }
        guiUpdate();

        checkCapitanAlive();

        doBasicMoveStrategy();

    }

    private void doBasicMoveStrategy() {

        if (shootActions()) {
            return;
        }

        if (medicActions()) {
            return;
        }

        if (moveActionsWithTarget()) {
            return;
        }

        if (moveActions()) {
            return;
        }
    }

    private void guiUpdate() {
        DrawPanel panel = guiFrame.panel;
        panel.updateContext(world, game, this);
        guiFrame.updateGraphics();


    }

    private void initTarget() {
        if (targetId == null) {
            if (!findTarget()) {
                return;
            }
        }
        for (Trooper trooper : troopers) {
            if (trooper.getId() == targetId) {
                target = trooper;
                return;
            }
        }
        log("Target lost!: " + targetId);
        target = null;
        targetId = null;

    }

    private boolean moveActionsWithTarget() {
        if (targetId == null) return false;
        if (self.getActionPoints() < game.getStandingMoveCost()) {
            return false;
        }
        move.setAction(ActionType.MOVE);
        moveTo(target, 2);
        return true;
    }

    private boolean fieldRationActions() {
        if (self.isHoldingFieldRation() &&
                10 - self.getActionPoints() > 4 && self.getActionPoints() > game.getFieldRationBonusActionPoints()) {
            move.setDirection(CURRENT_POINT);
            move.setAction(ActionType.EAT_FIELD_RATION);
            log("EAT RATION");
            return true;
        }
        return false;
    }

    private void checkCapitanAlive() {

        for (Trooper trooper : troopers) {
            if (trooper.getId() == capitanId) {
                return;
            }
        }
        log("!!!Change capitan!!!");
        Trooper capitan = null;
        for (Trooper trooper : troopers) {
            if (trooper.isTeammate() && (capitan == null || getDistance(new Point(capitan), movePoints.get(movePointIndex))
                    > getDistance(new Point(trooper), movePoints.get(movePointIndex)))) {
                capitanId = trooper.getId();
                capitan = trooper;
                // return;
            }
        }

        /*
        for (Trooper trooper : troopers) {
            if (trooper.isTeammate() && trooper.getType() == TrooperType.SOLDIER) {
                capitanId = trooper.getId();
                return;
            }
        }
        for (Trooper trooper : troopers) {
            if (trooper.isTeammate() && trooper.getType() == TrooperType.SCOUT) {
                capitanId = trooper.getId();
                return;
            }
        }
        for (Trooper trooper : troopers) {
            if (trooper.isTeammate() && trooper.getType() == TrooperType.SNIPER) {
                capitanId = trooper.getId();
                return;
            }
        }
        for (Trooper trooper : troopers) {
            if (trooper.isTeammate() && trooper.getType() == TrooperType.FIELD_MEDIC) {
                capitanId = trooper.getId();
                return;
            }
        }
         */

    }

    private void log(String s) {
        //     if (self.getType() == TrooperType.COMMANDER)
        System.out.println(world.getMoveIndex() + " |  " + self.getType() + " AP: " + self.getActionPoints() + " : " + s);

    }

    private void firstTimeInit() {
        initWayPoints();
        for (Trooper trooper : troopers) {
            if (trooper.isTeammate() && trooper.getType() == TrooperType.SOLDIER) {
                capitanId = trooper.getId();
            }
        }
        setFirstMoveIndex();


        //  troopers[0].

    }

    private void createGUI() {
                     /*
        guiFrame = new GUIFrame(world, game, this);
        guiFrame.toFront();
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setVisible(true);
       */

    }

    private void setFirstMoveIndex() {
        Point tmpPoint = null;
        for (Point point : movePoints) {
            if ((tmpPoint == null || getDistance(point, new Point(troopers[0])) <
                    getDistance(tmpPoint, new Point(troopers[0])))
                //   && movePoints.indexOf(point) != movePoints.size() -1
                //   && getDistance(point, new Point(troopers[0])) > 4
                    ) {
                tmpPoint = point;
                movePointIndex = movePoints.indexOf(tmpPoint);
            }
        }
        log("tmpPoint" + tmpPoint);
        log("movepointIndex: " + movePointIndex);

    }

    private boolean moveActions() {
        if (self.getActionPoints() < game.getStandingMoveCost()) {
            return false;
        }

        if (self.getId() != capitanId && dismissMove()) {
            return true;
        }

        if (moveToMedic()) {
            return true;
        }
        if (pickUpActions()) {
            log("picking up Bonus! " + move.getDirection() + " " + move.getAction());
            return true;
        }

        move.setAction(ActionType.MOVE);
        if (self.getId() == capitanId) {
            moveActionsCaptain();
            return true;
        } else {
            for (Trooper trooper : world.getTroopers()) {
                if (trooper.isTeammate() && trooper.getId() == capitanId) {
                    moveTo(getNearPoint(trooper), 0);
                    return true;
                }
            }
        }


        return false;
    }

    private boolean dismissMove() {
        if (!(world.getMoveIndex() == dismissMoveIndex || world.getMoveIndex() == dismissMoveIndex + 1)) {
            return false;
        }
        Point captainTarget = null;
        for (Trooper trooper : world.getTroopers()) {
            if (trooper.isTeammate() && trooper.getId() == capitanId) {
                captainTarget = new Point(trooper);
            }
        }
        if (captainTarget == null) {
            return false;
        }
        Point movePoint = new Point(0, 0);
        movePoint.x = (self.getX() - captainTarget.x) + self.getX();
        movePoint.y = (self.getY() - captainTarget.y) + self.getY();
        if (movePoint.x < 0) {
            movePoint.x = 0;
        }
        if (movePoint.x > world.getWidth() - 1) {
            movePoint.x = world.getWidth() - 1;
        }

        if (movePoint.y < 0) {
            movePoint.y = 0;
        }
        if (movePoint.y > world.getHeight() - 1) {
            movePoint.y = world.getHeight() - 1;
        }
        if (getDistance(new Point(self), captainTarget) >= 3) {
            return false;
        }
        move.setAction(ActionType.MOVE);
        moveTo(movePoint, 0);

        log("DUE DISMISS COMMAND!: " + new Point(self) + " movePoint: " + movePoint + " captain: " + captainTarget);
        return true;
    }

    private boolean moveToMedic() {
        if (self.getHitpoints() < self.getMaximalHitpoints() - DAMAGE_TERPIM && self.getType() != TrooperType.FIELD_MEDIC) {
            for (Trooper trooper : troopers) {
                if (trooper.isTeammate() && trooper.getType() == TrooperType.FIELD_MEDIC) {
                    move.setAction(ActionType.MOVE);
                    moveTo(getNearPoint(trooper), 0);
                    log("GO TO MEDIC!!!!!!! " + move.getAction() + " " + move.getDirection());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean pickUpActions() {

        Bonus haveBonusToPickUp = null;
        List<BonusType> holdingBonuses = new ArrayList<BonusType>();
        if (self.isHoldingMedikit())
            holdingBonuses.add(BonusType.MEDIKIT);
        if (self.isHoldingFieldRation())
            holdingBonuses.add(BonusType.FIELD_RATION);
        if (self.isHoldingGrenade())
            holdingBonuses.add(BonusType.GRENADE);

        for (Bonus bonus : bonuses) {
            // log(" bonus: " + bonus.getType() + " " + getDistance(self, bonus) + " have: " + holdingBonuses + " holdingBonuses.contains(bonus.getType()) " + !holdingBonuses.contains(bonus.getType()) );
            if (haveBonusToPickUp == null && !holdingBonuses.contains(bonus.getType()) && getDistance(self, bonus) <= 2
                    && testAccessability(bonus)) {
                haveBonusToPickUp = bonus;
            }
        }
        if (haveBonusToPickUp != null) {
            move.setAction(ActionType.MOVE);
            moveTo(haveBonusToPickUp, 1);
            log("MOVE TO BONUS: " + haveBonusToPickUp.getType() + " " + move.getAction());
            return true;
        }
        return false;
    }

    private boolean testAccessability(Unit unit) {
        return testAccessability(new Point(unit));
    }

    private void moveActionsCaptain() {
        if (self.getActionPoints() <= game.getStandingMoveCost() * 4 || someNeedHeal() /*&& teammateSoFar()*/) {
            move.setAction(ActionType.END_TURN);
            return;
        }
        /*
        if (medicNoFullHp()) {
            move.setAction(ActionType.END_TURN);
            return;
        }        */

        if (getDistance(new Point(self), movePoints.get(movePointIndex)) < 3) {
            movePointIndex++;
            if (movePointIndex == movePoints.size()) {
                movePointIndex = 0;
            }
        }

        moveTo(movePoints.get(movePointIndex), 2);
        checkUnstuck();

    }

    private void checkUnstuck() {
        if (savedPositions == null) {
            savedPositions = new ArrayList<Point>();
        }
        savedPositions.add(new Point(self));
        if (savedPositions.size() > 5) {
            double distance = 0;
            distance = getDistance(savedPositions.get(savedPositions.size() - 1), savedPositions.get(savedPositions.size() - 5));

            log("distance run: " + distance);
            if (distance < DISMIS_LENGTH_DISTANCE) {
                log("РАЗОЙДИСЬ");
                dismiss();
            }
        }

    }

    private void dismiss() {
        dismissMoveIndex = world.getMoveIndex();

    }

    private boolean teammateSoFar() {
        for (Trooper trooper : troopers) {
            if (self.getDistanceTo(trooper) > MAX_DISTANCE && trooper.isTeammate()) {
                return false;
            }
        }
        return false;
    }

    private boolean someNeedHeal() {

        if (medicAlive()) {
            for (Trooper trooper : troopers) {
                if (trooper.getHitpoints() < trooper.getMaximalHitpoints() && trooper.isTeammate()) {
                    log("wait until all heal");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean medicAlive() {
        for (Trooper trooper : troopers) {
            if (trooper.isTeammate() && trooper.getType() == TrooperType.FIELD_MEDIC) {
                return true;
            }
        }
        return false;
    }

    private boolean medicNoFullHp() {
        for (Trooper trooper : troopers) {
            if (trooper.isTeammate() && trooper.getType() == TrooperType.FIELD_MEDIC && trooper.getHitpoints() < trooper.getMaximalHitpoints()) {
                log("WAIT MEDIC UNTIL HE HEALED");
                move.setAction(ActionType.MOVE);
                moveTo(getNearPoint(trooper), 0);
                return true;
            }
        }
        return false;

    }

    private boolean moveTo(Point point, int distance) {
        if (self.getX() == point.x && self.getY() == point.y) {
            move.setAction(ActionType.END_TURN);
            return true;
        }
        if (getDistance(new Point(self.getX(), self.getY()), point) >= distance) {
            double deltaX = (point.x - self.getX());
            double deltaY = (point.y - self.getY());
            move.setDirection(CURRENT_POINT);
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                //   log("testAccess " + (testAccessability(new Point(self.getX() + (deltaX > 0 ? 1 : -1), self.getY()))));
                if (testAccessability(new Point(self.getX() + (deltaX > 0 ? 1 : -1), self.getY()))) {
                    move.setDirection((deltaX > 0 ? EAST : WEST));
                } else if (testAccessability(new Point(self.getX(), self.getY() + (deltaY > 0 ? 1 : -1)))) {
                    move.setDirection((deltaY > 0 ? SOUTH : NORTH));
                } else {
                    move.setDirection((deltaY > 0 ? NORTH : SOUTH));
                }
            } else {
                if (testAccessability(new Point(self.getX(), self.getY() + (deltaY > 0 ? 1 : -1)))) {
                    move.setDirection((deltaY > 0 ? SOUTH : NORTH));
                } else if (testAccessability(new Point(self.getX() + (deltaX > 0 ? 1 : -1), self.getY()))) {
                    move.setDirection((deltaX > 0 ? EAST : WEST));
                } else {
                    move.setDirection((deltaX > 0 ? WEST : EAST));
                }
            }
            //    checkMakedMove();
            //   log(move.getDirection() + " target: " + point + " self" + new Point(self) + " deltaX Y " + deltaX + " " + deltaY);
        }
        return true;

    }

    private void checkMakedMove() {
        boolean currentMoveCorrect = false;
        if (move.getDirection() == EAST) {
            currentMoveCorrect = testAccessability(new Point(self.getX() - 1, self.getY()));
        }

    }

    private boolean testAccessability(Point point) {

        CellType[][] cellTypes = world.getCells();
        if (point.x < world.getWidth() && point.y < world.getHeight() && point.x >= 0 & point.y >= 0)
            if (cellTypes[point.x][point.y] == CellType.FREE) {
                for (Trooper trooper : troopers) {
                    if (trooper.getX() == point.x && trooper.getY() == point.y) {
                        //          log("trooper block cell" + point);
                        return false;
                    }
                }
                //    log("cell free " + point);
                return true;
            }

        //    log("cell not free: " + point + " cellType: " + (point.x >= 0 && point.y >= 0
        //          && point.x < world.getWidth() && point.y < world.getHeight() ? cellTypes[point.x][point.y] : ""));
        return false;


    }

    private void moveY(Point point) {
        if (self.getY() > point.y) {
            move.setDirection(NORTH);
        } else {
            move.setDirection(SOUTH);
        }
    }

    private boolean testYAccessability(Point point) {
        int y = self.getY();
        if (self.getY() > point.y) {
            y--;
        } else {
            y++;
        }
        CellType[][] cellTypes = world.getCells();
        if (cellTypes[self.getX()][y] == CellType.FREE) {
            return true;
        }

        for (Trooper trooper : troopers) {
            if (trooper.getY() == y && trooper.getX() == self.getX()) {
                return false;
            }
        }
        return true;
    }


    private boolean testXAccessability(Point point) {
        int x = self.getX();
        if (self.getX() > point.x) {
            x--;
        } else if (self.getX() < point.x) {
            x++;
        }
        CellType[][] cellTypes = world.getCells();
        if (cellTypes[x][self.getY()] == CellType.FREE) {
            return true;
        }
        for (Trooper trooper : troopers) {
            if (trooper.getX() == x && trooper.getY() == self.getY()) {
                return false;
            }
        }
        return false;

    }


    private boolean shootActions() {
        if (target == null) {
            return false;
        }
        if (fieldRationActions()) {
            return true;
        }
        if (canShoot(world, self, target))
            if (self.getActionPoints() >= self.getShootCost()) {
                if (self.isHoldingGrenade() && getDistance(self, target) <= 5 && target.getHitpoints() >= MIN_HP_TO_THROW_GRENADE && self.getActionPoints() >= game.getGrenadeThrowCost()) {
                    move.setAction(ActionType.THROW_GRENADE);
                    log("THROW GRENADE");
                } else {
                    move.setAction(ActionType.SHOOT);
                }
                move.setX(target.getX());
                move.setY(target.getY());
                log(" shoot in trooper: " + target.getId() + " trooper:" + target + " self.getActionPoints()" + self.getActionPoints() + " getShootCost: " + self.getShootCost() + " move Action: " + move.getAction());
                return true;

            }
        return false;
    }

    private boolean attackMainTarget() {
        if (targetId == null) {

            if (!findTarget()) {
                log(" target not found");
                return false;
            }
        }
        if (self.getActionPoints() < self.getShootCost()) {
            return false;
        }
        for (Trooper trooper : world.getTroopers()) {
            if (trooper.getId() == targetId.longValue() && canShoot(world, self, trooper)) {
                if (self.isHoldingGrenade() && getDistance(self, trooper) < game.getGrenadeThrowCost()
                        && trooper.getHitpoints() >= MIN_HP_TO_THROW_GRENADE && self.getActionPoints() > game.getGrenadeThrowCost()) {
                    move.setAction(ActionType.THROW_GRENADE);
                    log("THROW GRENADE" + trooper.getId());
                } else {
                    log(" shoot in trooper: " + trooper.getId());
                    move.setAction(ActionType.SHOOT);
                }
                move.setX(trooper.getX());
                move.setY(trooper.getY());

                return true;
            }
        }
        targetId = null;
        return attackMainTarget();
        // return false;

    }

    private boolean findTarget() {
        for (Trooper trooper : world.getTroopers()) {
            if (!trooper.isTeammate() && canShoot(world, self, trooper) && trooper.getType() == TrooperType.FIELD_MEDIC && (target == null || getDistance(self, trooper) < getDistance(self, target))) {
                targetId = trooper.getId();
                log("MEDICTarget found! " + targetId);
                target = trooper;
            }
        }
        if (target == null)
            for (Trooper trooper : world.getTroopers()) {
                if (!trooper.isTeammate() && canShoot(world, self, trooper) && (target == null || getDistance(self, trooper) < getDistance(self, target))) {
                    targetId = trooper.getId();
                    log("Target found! " + targetId);
                    target = trooper;
                }
            }
        if (target != null) {
            return true;
        } /*
        for (Trooper trooper : world.getTroopers()) {
            if (!trooper.isTeammate() && (target == null || getDistance(self, trooper) < getDistance(self, target))) {
                targetId = trooper.getId();
                log("Target found! " + targetId);
                target = trooper;
            }
        }
        if (target != null) {
            return true;
        }  */
        return false;
    }

    private boolean medicActions() {
        // FOR EVERY ONE
        if (self.isHoldingMedikit() && self.getActionPoints() >= game.getMedikitUseCost()) {
            if (self.getMaximalHitpoints() - self.getHitpoints() >= game.getMedikitBonusHitpoints()) {
                move.setAction(ActionType.USE_MEDIKIT);
                move.setDirection(CURRENT_POINT);
                //   log(" MEDKIT HIM SELF currenthp: " + self.getHitpoints() + "  + " + game.getMedikitBonusHitpoints());
                return true;
            }
            if (closeHeal()) {
                return true;
            }
        }


        // FOR MEDIC
        if (medicHasOne()) {
            if (shootActions()) {
                return true;
            }
        }


        if (self.getType() == TrooperType.FIELD_MEDIC && self.getActionPoints() >= game.getFieldMedicHealCost()) {
            if (self.getHitpoints() < self.getMaximalHitpoints()) {
                move.setAction(ActionType.HEAL);
                move.setDirection(CURRENT_POINT);
                System.out.println("tick: " + world.getMoveIndex() + " medic heal himself currenthp: " + self.getHitpoints() + "  + " + game.getMedikitBonusHitpoints());
                return true;
            }
            for (Trooper trooper : world.getTroopers()) {
                if (trooper.isTeammate()) {
                    if (trooper.getHitpoints() < trooper.getMaximalHitpoints()) {
                        if (getDistance(self, trooper) == 1) {
                            move.setAction(ActionType.HEAL);
                            move.setDirection(getDirectionTo(self, trooper));
                            return true;
                        }
                    }
                }
            }
            if (goToHealTeammatte()) {
                return true;
            }
        }
        return false;
    }

    private boolean medicHasOne() {
        for (Trooper trooper : troopers) {
            if (trooper.isTeammate() && trooper.getType() != TrooperType.FIELD_MEDIC) {
                return false;
            }
        }
        return true;
    }

    private boolean goToHealTeammatte() {
        if (self.getType() == TrooperType.FIELD_MEDIC)
            for (Trooper trooper : troopers) {
                if (trooper.isTeammate() && trooper.getType() != TrooperType.FIELD_MEDIC
                        && trooper.getHitpoints() < trooper.getMaximalHitpoints()) {
                    move.setAction(ActionType.MOVE);
                    moveTo(getNearPoint(trooper), 0);
                    log("medic go to the trooper! : " + trooper.getType() + " " + move.getAction() + " " + move.getDirection());
                    return true;
                }
            }
        return false;
    }

    private Point getNearPoint(Trooper trooper) {
        List<Point> points = new ArrayList<Point>();
        List<Point> pointsToRemove = new ArrayList<Point>();
        points.add(new Point(trooper.getX() + 1, trooper.getY()));
        points.add(new Point(trooper.getX(), trooper.getY() + 1));
        points.add(new Point(trooper.getX() - 1, trooper.getY()));
        points.add(new Point(trooper.getX(), trooper.getY() - 1));

        for (Point point : points) {
            if (point.x == self.getX() && point.y == self.getY()) {
                continue;
            }
            if (!testAccessability(point)) {
                pointsToRemove.add(point);
            }
        }

        points.removeAll(pointsToRemove);

        Point result = null;
        for (Point point : points) {
            if (result == null || getDistance(new Point(self), point) < getDistance(new Point(self), result)) {
                result = point;
            }
        }

        return (result == null ? new Point(trooper) : result);
    }

    private boolean closeHeal() {
        for (Trooper trooper : world.getTroopers()) {
            if (trooper.isTeammate()) {
                if (trooper.getMaximalHitpoints() - trooper.getHitpoints() >= game.getMedikitBonusHitpoints()) {
                    if (getDistance(self, trooper) == 1) {
                        move.setAction(ActionType.USE_MEDIKIT);
                        move.setDirection(getDirectionTo(self, trooper));
                        //              log(" MEDKIT on trooper " + trooper.getType() + " currenthp: " + self.getHitpoints() + "  + " + game.getMedikitBonusHitpoints());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void moveX(Point point) {
        if (self.getX() > point.x) {
            move.setDirection(WEST);
        } else if (self.getX() < point.x) {
            move.setDirection(EAST);
        }
    }

    private Direction getDirectionTo(Trooper trooper1, Trooper trooper2) {
        double deltaX = Math.abs(trooper1.getX() - trooper2.getX());
        double deltaY = Math.abs(trooper1.getY() - trooper2.getY());

        if (deltaX > deltaY) {
            if (trooper1.getX() > trooper2.getX()) {
                return WEST;
            } else if (trooper1.getX() < trooper2.getX()) {
                return EAST;
            }
        } else {
            if (trooper1.getY() > trooper2.getY()) {
                return NORTH;
            } else {
                return SOUTH;
            }
        }
        return null;
    }

    private boolean moveTo(Unit unit, int distance) {
        return moveTo(new Point(unit.getX(), unit.getY()), distance);
    }


    private void init(Trooper self, World world, Game game, Move move) {
        this.self = self;
        this.world = world;
        this.game = game;
        this.move = move;
        this.troopers = world.getTroopers();
        this.bonuses = world.getBonuses();
    }

    private void initWayPoints() {

        movePoints = new ArrayList<Point>();
        if (jugnleMap()) {
            movePoints.add(new Point(0, world.getHeight()));
            movePoints.add(new Point(world.getWidth() / 4, world.getHeight() / 2));
            movePoints.add(new Point(0, 0));
            movePoints.add(new Point(world.getWidth(), 0));
            movePoints.add(new Point((world.getWidth() / 4) * 3, world.getHeight() / 2));
            movePoints.add(new Point(world.getWidth(), world.getHeight()));
            //movePoints.add(new Point(world.getWidth() / 2, world.getHeight() / 2));
        }/* else if (cheaserMap()) {
            movePoints.add(new Point(0, 0));
            movePoints.add(new Point(7, 1));
            movePoints.add(new Point(10, 3));
            movePoints.add(new Point(11, 4));
            movePoints.add(new Point(12, 3));
            movePoints.add(new Point(world.getWidth()/2, 3));
            movePoints.add(new Point(16, 3));

        } */ else {
            movePoints.add(new Point(0, world.getHeight()));
            movePoints.add(new Point(0, 0));
            movePoints.add(new Point(world.getWidth(), 0));
            movePoints.add(new Point(world.getWidth(), world.getHeight()));
            //   movePoints.add(new Point(world.getWidth() / 2, world.getHeight() / 2));
        }
    }

    private boolean cheaserMap() {
        if (world.getCells()[2][2] != CellType.FREE && world.getCells()[1][5] != CellType.FREE) {
            return true;
        }
        return false;
    }

    private boolean jugnleMap() {

        return world.getCells()[4][0] != CellType.FREE && world.getCells()[4][2] != CellType.FREE;
    }


    public boolean
    canShoot(World world, Trooper self, Trooper targetTrooper) {

        return world.isVisible(getCorrectedShootingRange(),
                self.getX(), self.getY(), self.getStance(),
                targetTrooper.getX(), targetTrooper.getY(), targetTrooper.getStance());

    }

    private double getCorrectedShootingRange() {
        if (self.getId() == capitanId) {
            //     log("corrected shooting range" + (self.getShootingRange() - 1) + " " + self.getShootingRange());
            return self.getShootingRange();
        }
        return self.getShootingRange();
    }

    private double getDistance(Unit unit1, Unit unit2) {
        return getDistance(unit1.getX(), unit1.getY(), unit2.getX(), unit2.getY());
    }

    private double getDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private double getDistance(Point point1, Point point2) {
        return getDistance(point1.x, point1.y, point2.x, point2.y);
    }


    private class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Point(Unit unit) {
            x = unit.getX();
            y = unit.getY();

        }

        @Override
        public String toString() {
            return "point: x: " + x + " y: " + y;
        }
    }
}
