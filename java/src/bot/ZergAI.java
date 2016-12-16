package bot;


import java.util.*;

import jnibwapi.*;
import jnibwapi.types.TechType;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType;

import jnibwapi.types.*;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * Example of a Java AI Client that does nothing.
 */
import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.util.BWColor;
import jnibwapi.Map;
import sun.management.counter.Units;

public class ZergAI implements BWAPIEventListener {
    private final JNIBWAPI bwapi;

    //units that are used in multiple functions
    private Unit poolProbe;
    private Unit gasProbe;
    private Unit nexus;
    private Unit geyser;
    private Unit minerals;
    private Unit gateway;

    //neutral unit positions
    Position geyserPosition;
    Position nexusPosition;
    Position mineralPosition;

    //our base region variable
    Region baseRegion;

    //our nearest choke point variable
    ChokePoint myChokePoint;

    //booleans to check if a building exists
    boolean pylonBuilt;
    boolean hasAssimilator;
    boolean hasGateway;
    boolean hasCyber;
    boolean hasCitadel;
    boolean hasArchives;
    boolean hasForge;

    //Build order
    int[] buildOrderArray = new int[7];
    int buildOrderIndex = 0;

    int probeCount = 0;
    int zealotCount = 0;
    //positioning for buildings
    Position buildPosition;
    Position pylonPosition;
    Position gatewayPosition;
    Position cyberPosition;
    Position citadelPosition;
    Position archivesPosition;
    Position forgePosition;
    private Set<Player> enemies;
    private RaceType enemy;

    public static void main(String[] args) {
        new MinimalAIClient();
    }

    public ZergAI() {
        bwapi = new JNIBWAPI(this, true);
        bwapi.start();
    }

    @Override
    public void connected() {
    }

    @Override
    public void matchStart() {
        System.out.println("Game Started");

        bwapi.enableUserInput();
        bwapi.enablePerfectInformation();

        bwapi.setGameSpeed(0);
        poolProbe = null;
        gasProbe = null;
        myChokePoint = null;
        pylonBuilt = false;
        hasAssimilator = false;
        hasGateway = false;
        hasCyber = false;
        hasCitadel = false;
        hasArchives = false;
        hasForge = false;
        for (Unit u : bwapi.getMyUnits()) {
            if (u.getType() == UnitTypes.Protoss_Nexus) {
                nexus = u;
            } else if (u.getType() == UnitTypes.Protoss_Probe && poolProbe == null) {
                poolProbe = u;
            } else if (u.getType() == UnitTypes.Protoss_Probe && gasProbe == null) {
                gasProbe = u;
            }
        }
        for (Unit u : bwapi.getNeutralUnits()) {
            baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
            if (u.getType().isMineralField() && bwapi.getMap().getRegion(u.getPosition()) == baseRegion) {
                minerals = u;
            }
        }

        //bwapi.setGameSpeed(0);

        // Determine what race the enemy is.

        buildOrderArray[0] = (UnitTypes.Protoss_Pylon.getID());
        //buildOrderArray[1] = (UnitTypes.Protoss_Assimilator.getID());
        buildOrderArray[1] = (UnitTypes.Protoss_Forge.getID());
        buildOrderArray[2] = (UnitTypes.Protoss_Pylon.getID());
        buildOrderArray[3] = (UnitTypes.Protoss_Photon_Cannon.getID());
        buildOrderArray[4] = (UnitTypes.Protoss_Pylon.getID());
        buildOrderArray[5] = (UnitTypes.Protoss_Photon_Cannon.getID());
        buildOrderArray[6] = (UnitTypes.Protoss_Gateway.getID());

        int closestChokePoint = 1000;
        for (ChokePoint chokePoint : bwapi.getMap().getChokePoints()) {
//                        System.out.println("Choke point: " + chokePoint.getCenter());
//                        System.out.println("Nexus: " + nexus.getPosition());
//                        System.out.println(nexus.getDistance(chokePoint.getCenter()));
            bwapi.drawCircle(chokePoint.getCenter(), 8, BWColor.Cyan, true, false);
            if (nexus.getDistance(chokePoint.getCenter()) < closestChokePoint) {
                myChokePoint = chokePoint;
            }
        }
    }

    /*
     * The game strategy for Zerg enemies.
     */
    public void protossVsZerg() {
        firstPylonPosition();
        int mineralCount = bwapi.getSelf().getMinerals();
        int gasCount = bwapi.getSelf().getGas();
        // supply used
        int supplyUsed = bwapi.getSelf().getSupplyUsed();
        //supply total
        int supplyTotal = bwapi.getSelf().getSupplyTotal();
        int buildOrderNumber = supplyUsed / 2;
        // build order

        zealotCount = 0;
        for (Unit u : bwapi.getMyUnits()) {
            collectMinerals(u);
            //collectGas(mineralCount,u);
            if (u.getType() == UnitTypes.Protoss_Zealot && u.isCompleted()) {
                zealotCount += 1;
                if (zealotCount >= 10) {
                    ZealotAttack();
                }
            } else {
                buildZealots(mineralCount);
            }
        }
        if (supplyUsed >= supplyTotal - 1) {

            //BuildBuilding(mineralCount, UnitTypes.Protoss_Pylon, placement(pylonPosition, 40, 500, 5));
        }
        if (buildOrderNumber < 7) {
            buildProbes(mineralCount);
            for (Unit minerals : bwapi.getNeutralUnits()) {
                baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
                if (minerals.getType().isMineralField() && bwapi.getMap().getRegion(minerals.getPosition()) == baseRegion) {
                    double distance = poolProbe.getDistance(minerals);
                    if (distance < 300) {
                        gasProbe.rightClick(minerals, false);
                        poolProbe.rightClick(minerals, false);
                        break;
                    }
                }
            }
        }
        for (Unit pylon : bwapi.getMyUnits()) {
            if (pylon.getType() == UnitTypes.Protoss_Pylon) {
                BuildBuilding(mineralCount, UnitTypes.getUnitType(buildOrderArray[buildOrderIndex]), placement(pylon.getPosition(), 40, 70, 5));
            } else {
                BuildBuilding(mineralCount, UnitTypes.getUnitType(buildOrderArray[buildOrderIndex]), placement(pylonPosition, 40, 70, 5));
            }
        }
        buildProbes(mineralCount);
    }
//                 * 15 - Pylon[4]
//                 * 18 - Nexus
//                 * 18 - Gateway [5]
//                 * 20 - Assimilator [6]
//                 * 22 - Cybernetics Core
//                 * 25/26 - Assimilator[7]
//                 * @ 100% Cybernetics Core - Dragoon[8]
//                 * 100% Cybernetics Core - Stargate
//                 * @ 100 Gas - Citadel of Adun[9]
//                 * @ 100 Gas - Corsair
//                 * @ 100 Gas - +1 Ground Attack
//                 * @ 200 Gas - Templar Archives
//                 * 3 Gateways
//                 * @ 100% Templar Archives - 2 Archons[10]
//                 * @ 2 Archons - Zealot Speedupgrade
//                 * @ ~95% +1 Attack Upgrade - Army moves out
//                 */
    //placement();
    //calling the functions in the matchframe


//                buildAssimilator(mineralCount);
//                collectMinerals();
//                collectGas();
//                buildProbes(mineralCount);
//                buildPylons(mineralCount, supplyUsed, supplyTotal);
//                placement();
//                pylonRadius();
//                buildGateway(mineralCount);
//                buildCitadel(mineralCount, gasCount);
//                buildCyber(mineralCount);
//                buildTemplarArchive(mineralCount);
//                buildDrag(mineralCount , gasCount);
//                buildZealots(mineralCount);
//    }

    private boolean buildingExists(UnitType buildingType, Position buildingPosition) {
        for (Unit u : bwapi.getMyUnits()) {
            if (u.getType() == buildingType) {
                int pos1 = u.getPosition().getBX();
                int pos2 = buildingPosition.getBX();
                if (Math.abs(pos1 - pos2) <= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * The game strategy for Protoss enemies.
     */
    private void protossVsProtoss() {
    }

    /*
     * This runs every game frame (multiple times a second!!)
     */
    @Override
    public void matchFrame() {
        protossVsZerg();
    }

    //a function to collect Mineral
    public void collectMinerals(Unit unit) {
        if (unit.getType() == UnitTypes.Protoss_Probe) {
            if (unit.isIdle() && unit != poolProbe && unit != gasProbe) {
                for (Unit minerals : bwapi.getNeutralUnits()) {
                    baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
                    if (minerals.getType().isMineralField() && bwapi.getMap().getRegion(minerals.getPosition()) == baseRegion) {
                        double distance = unit.getDistance(minerals);
                        if (distance < 300) {
                            unit.rightClick(minerals, false);
                            break;
                        }
                    }
                }
            }
        }
    }

    // a Function to collect Gas
    public void collectGas(int mineralCount, Unit unit) {
        baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
        for (Unit refine : bwapi.getNeutralUnits()) {
            if (refine.getType() == UnitTypes.Resource_Vespene_Geyser && bwapi.getMap().getRegion(refine.getPosition()) == baseRegion) {
                if (refine.getType().isRefinery()) {
                    double distance = unit.getDistance(refine);
                    if (distance < 300) {
                        unit.gather(refine, true);
                        bwapi.drawCircle(unit.getPosition(), 8, BWColor.Yellow, true, false);
                        break;
                    }
                } else {
                    bwapi.drawCircle(refine.getTopLeft(), 8, BWColor.Yellow, true, false);
//                    BuildBuilding(mineralCount, UnitTypes.Protoss_Assimilator, refine.getTopLeft());
                }
            }

        }
    }

    //a function to build the assimilator
    public void buildAssimilator(int mineralCount) {
        if (poolProbe != null && !hasAssimilator && mineralCount >= 100) {
            for (Unit vespene : bwapi.getNeutralUnits()) {
                baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
                if (vespene.getType() == UnitTypes.Resource_Vespene_Geyser && bwapi.getMap().getRegion(vespene.getPosition()) == baseRegion) {
                    geyserPosition = vespene.getTilePosition();
                    break;
                }
            }
            if (poolProbe.isIdle()) {
                poolProbe.build(geyserPosition, UnitTypes.Protoss_Assimilator);
                // The below is not good enough logic to ensure a building is actually being constructed.
                // you should make sure you have an assmilitor in construction before changing hasAssimilator
                hasAssimilator = true;
            }
        }
        for (Unit u : bwapi.getAllUnits()) {
            if (geyserPosition != null) {
                bwapi.drawCircle(geyserPosition, 5, BWColor.Yellow, true, false);
            }
            bwapi.drawCircle(u.getPosition(), 5, BWColor.Red, true, false);
        }
    }

    public void BuildBuilding(int mineralCount, UnitType building, Position position) {
        if (mineralCount > building.getMineralPrice()) {
            poolProbe.build(position, building);
        }
    }

    //function to build probes
    public void buildProbes(int mineralCount) {
        if (mineralCount >= 50 && !nexus.isTraining() &&
                bwapi.getSelf().getSupplyUsed() / 2 <= bwapi.getSelf().getSupplyTotal() / 2 && probeCount < 15) {
            probeCount += 1;
            nexus.train(UnitTypes.Protoss_Probe);
        }
    }

    //function to create dragoons
    public void buildDrag(int mineralCount, int gasCount) {
        if (hasCyber && mineralCount > 300 && gasCount > 50) {
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Protoss_Gateway) {
                    gateway = unit;
                    gateway.train(UnitTypes.Protoss_Dragoon);
                }
            }
        }
    }

    public void ZealotAttack() {
        for (Unit u : bwapi.getMyUnits()) {
            if (u.getType() == UnitTypes.Protoss_Zealot && u.isCompleted()) {
                for (Unit enemy : bwapi.getEnemyUnits()) {
                    u.attack(enemy.getPosition(), true);
                }
            }
        }
    }

    //Generic function to train units
//        public void TrainUnit(UnitType unitToBuild, Unit Building) {
//            Building.train(unitToBuild);
//        }
    //function to create zealots
    public void buildZealots(int mineralCount) {
        for (Unit unit : bwapi.getMyUnits()) {
            if (unit.getType() == UnitTypes.Protoss_Gateway) {
                gateway = unit;
                gateway.train(UnitTypes.Protoss_Zealot);
            }
        }
    }

    //function trying to find the radius of the pylon
    public void pylonRadius(Position pylonPoint) {
        for (Unit pylon : bwapi.getMyUnits()) {
            if (pylon.getType() == UnitTypes.Protoss_Pylon && pylon.getPosition() == pylonPoint) {
                Position top = pylon.getTopLeft();
                //  System.out.print("This is top left: "+top);
                //  bwapi.drawBox(top, 5, BWColor.Blue, true, false);
                Position bot = pylon.getBottomRight();
                // System.out.print("This is bottom left: "+bot);
                bwapi.drawBox(top, bot, BWColor.Yellow, true, false);
            }
        }
    }

    public void firstPylonPosition() {

        baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
        nexusPosition = nexus.getPosition();

        int xBuild = nexusPosition.getX(Position.PosType.PIXEL);
        int yBuild = nexusPosition.getY(Position.PosType.PIXEL);

        mineralPosition = minerals.getPosition();

        int xMin = mineralPosition.getX(Position.PosType.PIXEL);
        int yMin = mineralPosition.getY(Position.PosType.PIXEL);

        int pixelPosiCounter = 50;
        int pixelNegaCounter = 50;
        if (xBuild > xMin) {
            //nexus right of minerals so build to the right
            //Add X value
            xBuild = xBuild + 100 + pixelPosiCounter;

        } else {
            //nexus left of minerals build to left
            //Subtract X value
            xBuild = xBuild - 200;
        }
        if (yBuild > yMin) {
            //nexus top of minerals so build to the top of nexus
            yBuild = yBuild + 100 + pixelPosiCounter;
        } else {
            //nexus bottom of minerals so build to the bottom of nexus
            //subtract Y value
            yBuild = yBuild - 200;
        }
        //euclidean distance to not build in this area
        pylonPosition = new Position(xBuild, yBuild);
        bwapi.drawCircle(pylonPosition, 8, BWColor.White, true, false);
    }

    public Position placement(Position start, int radius, int max, int inc) {
        int checkPointX = start.getX(Position.PosType.PIXEL);
        int checkPointY = start.getY(Position.PosType.PIXEL);
        Position checkPosition1;
        Position checkPosition2;
        Position checkPosition3;
        Position checkPosition4;
        Position checkPosition5;
        Position checkPosition6;
        Position checkPosition7;
        Position checkPosition8;

        buildPosition = pylonPosition;
        for (int x_offset = radius; x_offset < max; x_offset += inc) {
            for (int y_offset = radius; y_offset <= (max); y_offset += inc) {
                checkPosition1 = new Position(checkPointX + x_offset, checkPointY + y_offset);
                checkPosition2 = new Position(checkPointX + x_offset, checkPointY - y_offset);
                checkPosition3 = new Position(checkPointX - x_offset, checkPointY + y_offset);
                checkPosition4 = new Position(checkPointX - x_offset, checkPointY - y_offset);
                checkPosition5 = new Position(checkPointX, checkPointY + y_offset);
                checkPosition6 = new Position(checkPointX, checkPointY - y_offset);
                checkPosition7 = new Position(checkPointX + x_offset, checkPointY);
                checkPosition8 = new Position(checkPointX - x_offset, checkPointY);

                //for each spot check the radius around it and then draw a circle
                if (bwapi.isBuildable(checkPosition1, true)) {
                    if (checkSpot(checkPosition1.getX(Position.PosType.PIXEL), checkPosition1.getY(Position.PosType.PIXEL)) == true) {
                        bwapi.drawCircle(checkPosition1, 3, BWColor.Yellow, true, false);
                        buildPosition = checkPosition1;
                        //System.out.println("Check Position = " + checkPosition1);
                        break;
                    }
                }
                if (bwapi.isBuildable(checkPosition2, true)) {
                    if (checkSpot(checkPosition2.getX(Position.PosType.PIXEL), checkPosition2.getY(Position.PosType.PIXEL)) == true) {
                        bwapi.drawCircle(checkPosition2, 3, BWColor.Yellow, true, false);
                        buildPosition = checkPosition2;
                        break;
                    }
                }
                if (bwapi.isBuildable(checkPosition3, true)) {
                    if (checkSpot(checkPosition3.getX(Position.PosType.PIXEL), checkPosition3.getY(Position.PosType.PIXEL)) == true) {
                        bwapi.drawCircle(checkPosition3, 3, BWColor.Yellow, true, false);
                        buildPosition = checkPosition3;
                        break;
                    }
                }
                if (bwapi.isBuildable(checkPosition4, true)) {
                    if (checkSpot(checkPosition4.getX(Position.PosType.PIXEL), checkPosition4.getY(Position.PosType.PIXEL)) == true) {
                        bwapi.drawCircle(checkPosition4, 3, BWColor.Yellow, true, false);
                        buildPosition = checkPosition4;
                        break;
                    }
                }
                if (bwapi.isBuildable(checkPosition5, true)) {
                    if (checkSpot(checkPosition5.getX(Position.PosType.PIXEL), checkPosition5.getY(Position.PosType.PIXEL)) == true) {
                        bwapi.drawCircle(checkPosition5, 3, BWColor.Yellow, true, false);
                        buildPosition = checkPosition5;
                        break;
                    }
                }
                if (bwapi.isBuildable(checkPosition6, true)) {
                    if (checkSpot(checkPosition6.getX(Position.PosType.PIXEL), checkPosition6.getY(Position.PosType.PIXEL)) == true) {
                        bwapi.drawCircle(checkPosition6, 3, BWColor.Yellow, true, false);
                        buildPosition = checkPosition6;
                        break;
                    }
                }
                if (bwapi.isBuildable(checkPosition7, true)) {
                    if (checkSpot(checkPosition7.getX(Position.PosType.PIXEL), checkPosition7.getY(Position.PosType.PIXEL)) == true) {
                        bwapi.drawCircle(checkPosition7, 3, BWColor.Yellow, true, false);
                        buildPosition = checkPosition7;
                        break;
                    }
                }
                if (bwapi.isBuildable(checkPosition8, true)) {
                    if (checkSpot(checkPosition8.getX(Position.PosType.PIXEL), checkPosition8.getY(Position.PosType.PIXEL)) == true) {
                        bwapi.drawCircle(checkPosition8, 3, BWColor.Yellow, true, false);
                        buildPosition = checkPosition8;
                        break;
                    }
                }
            }
            if (buildPosition != null) {
                break;
            }
        }
        //System.out.println("Build Position = " + buildPosition);
        return buildPosition;
    }

    public boolean checkSpot(int checkX, int checkY) {
        Position checkPosition1;
        Position checkPosition2;
        Position checkPosition3;
        Position checkPosition4;
        Position checkPosition5;
        Position checkPosition6;
        Position checkPosition7;
        Position checkPosition8;

        //max is the radius around things
        int max = 70;
        int offset = 5;
        for (int x_offset = 0; x_offset < max; x_offset += offset) {
            for (int y_offset = 0; y_offset <= (max); y_offset += offset) {
                checkPosition1 = new Position(checkX + x_offset, checkY + y_offset);
                checkPosition2 = new Position(checkX + x_offset, checkY - y_offset);
                checkPosition3 = new Position(checkX - x_offset, checkY + y_offset);
                checkPosition4 = new Position(checkX - x_offset, checkY - y_offset);
                checkPosition5 = new Position(checkX, checkY + y_offset);
                checkPosition6 = new Position(checkX, checkY - y_offset);
                checkPosition7 = new Position(checkX + x_offset, checkY);
                checkPosition8 = new Position(checkX - x_offset, checkY);

                //If it is true for all then continue
                if (bwapi.isBuildable(checkPosition1, true) && bwapi.isBuildable(checkPosition2, true) && bwapi.isBuildable(checkPosition3, true) && bwapi.isBuildable(checkPosition4, true) && bwapi.isBuildable(checkPosition5, true) && bwapi.isBuildable(checkPosition6, true) && bwapi.isBuildable(checkPosition7, true) && bwapi.isBuildable(checkPosition8, true)) {

                }
                //else break and say it is not buildable
                else {
                    return false;
                }
            }
        }
        //return true since nothing returned false
        return true;
    }

    public void terranEnemy() {
        //    if( int dragCount >9 && enemyTerran){
        //  }
    }

    public void protossEnemy() {
        //  if( dragCount >9 && enemyTerran){
        //  }
    }

    @Override
    public void keyPressed(int keyCode) {
    }

    @Override
    public void matchEnd(boolean winner) {
    }

    @Override
    public void sendText(String text) {
    }

    @Override
    public void unitCreate(int unitID) {
        if (UnitTypes.getUnitType(buildOrderArray[buildOrderIndex]) == (bwapi.getUnit(unitID)).getType()) {
            buildOrderIndex += 1;
            System.out.println("FDKJSAHFJKADH;A");
        } else if (buildOrderIndex == buildOrderArray.length) {
            buildOrderIndex = 0;
        }
    }

    @Override
    public void receiveText(String text) {
    }

    @Override
    public void nukeDetect(Position p) {
    }

    @Override
    public void nukeDetect() {
    }

    @Override
    public void playerLeft(int playerID) {
    }

    @Override
    public void unitDestroy(int unitID) {
    }

    @Override
    public void unitDiscover(int unitID) {
    }

    @Override
    public void unitEvade(int unitID) {
    }

    @Override
    public void unitHide(int unitID) {
    }

    @Override
    public void unitMorph(int unitID) {
    }

    @Override
    public void unitShow(int unitID) {
    }

    @Override
    public void unitRenegade(int unitID) {
    }

    @Override
    public void saveGame(String gameName) {
    }

    @Override
    public void unitComplete(int unitID) {

    }

    @Override
    public void playerDropped(int playerID) {
    }
}