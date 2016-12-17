package bot;

import jnibwapi.*;
import jnibwapi.types.UnitType;
import java.util.Iterator;
import java.util.Set;
import jnibwapi.types.*;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.util.BWColor;
/*--------------------------------------------------------------------
|  Class VSZergAI
|
|  Purpose: Implementation of the VSZerg strategy.
|  Mostly aimed at trying to survive early aggressive Zerg strategies.
*-------------------------------------------------------------------*/
public class VSZergAI implements  BWAPIEventListener{

    private JNIBWAPI bwapi;
    private Unit poolProbe;
    private Unit gasProbe;
    private Unit gasProbe2;
    private Unit gasProbe3;
    private Unit nexus;
    private Unit enemyAttack;
    private Unit geyser;
    private Unit minerals;
    private Unit gateway;
    private Unit zealots;

    // Neutral unit positions
    Position geyserPosition;
    Position nexusPosition;
    Position mineralPosition;
    Position enemyPosition;
    Position gatewayPosition;
    Position cyberPosition;
    Position citadelPosition;
    Position archivesPosition;
    Position forgePosition;
    Position pylonPosition2;
    Position pylonPosition3;
    Position pylonPosition4;

    // Choke point positions
    Position center;
    Position firstChoke;
    Position secondChoke;

    // Number of units we plan to send to attack the enemy
    private int dragoonAttackCount = 6;
    private int zealotAttackCount = 6;
    private int positionCounter =0;

    // Queues for the buildings
    private int zealotQueue = 0;
    private int dragoonQueue = 0;

    // Our base region variable
    Region baseRegion;

    // Positioning for buildings
    Position pylonPosition;
    Position buildPosition;

    // Enemy player
    private Set<Player> enemies;
    private RaceType enemy;

    // Different counts we need
    public int mineralCount;
    private int gasCount;
    private int supplyUsed;
    private int supplyTotal;
    private int probeCount;

    //booleans to check if a building exists
    boolean pylonBuilt;
    boolean hasAssimilator;
    boolean hasGateway;
    boolean hasCyber;
    boolean hasCitadel;
    boolean hasArchives;
    boolean hasForge;
    /*--------------------------------------------------------------------
    |  Method protossVsZerg
    |
    |  Purpose: Implement build order for protoss vs Zerg
    |   See gitHub readme for complete build order
    |      explanation.
    *-------------------------------------------------------------------*/
    public void protossVsZerg(JNIBWAPI bwapi) {

        //initializing all the variables
        bwapi.enableUserInput();
        bwapi.enablePerfectInformation();
        // Get hardcoded building placements.
        placement();
        //bwapi.setGameSpeed(0);
        poolProbe = null;
        gasProbe = null;
        gasProbe2 = null;
        zealots = null;

        for (Unit u : bwapi.getMyUnits()) {
            if (u.getType() == UnitTypes.Protoss_Nexus) {
                nexus = u;
            } else if (u.getType() == UnitTypes.Protoss_Probe && poolProbe == null) {
                poolProbe = u;
            }
        }
        for (Unit u : bwapi.getNeutralUnits()) {
            baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
            if (u.getType().isMineralField() && bwapi.getMap().getRegion(u.getPosition()) == baseRegion) {
                minerals = u;
            }
        }

        this.bwapi = bwapi;
        int mineralCount = bwapi.getSelf().getMinerals();
        int gasCount = bwapi.getSelf().getGas();
        // supply used
        int supplyUsed = bwapi.getSelf().getSupplyUsed();
        //supply total
        int supplyTotal = bwapi.getSelf().getSupplyTotal();
        int zealotCounter = 0;
        int buildOrderNumber = supplyUsed / 2;
        // Attack test for PvZ.
        for (Unit u : bwapi.getMyUnits()) {
            if(u.getType() == UnitTypes.Protoss_Gateway && u.isCompleted()) {
                u.train(UnitTypes.Protoss_Zealot);
            }
            if (u.getType() == UnitTypes.Protoss_Zealot && u.isCompleted()) {
                zealotCounter += 1;
                if (zealotCounter > 7) {
                    for (Unit zealot : bwapi.getMyUnits()) {
                        if (zealot.getType() == UnitTypes.Protoss_Zealot && zealot.isCompleted()) {

                            for (Unit enemy : bwapi.getEnemyUnits()) {
                                zealot.attack(enemy.getPosition(), true);
                            }
                        }

                    }
                }
            }
        }
        if (supplyUsed >= supplyTotal-5) {
            if (supplyUsed > 20) {
                buildPylons(mineralCount,pylonPosition3);
            }
            if (supplyUsed > 30) {
                buildPylons(mineralCount,pylonPosition4);
            }
            buildPylons(mineralCount,pylonPosition2);
        }
        // build order
        collectMinerals();
        if (buildOrderNumber < 7) {
            buildProbes(mineralCount);
            for (Unit minerals : bwapi.getNeutralUnits()) {
                baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
                if (minerals.getType().isMineralField() && bwapi.getMap().getRegion(minerals.getPosition()) == baseRegion) {
                    double distance = poolProbe.getDistance(minerals);
                    if (distance < 300) {
                        gasProbe.rightClick(minerals,false);
                        poolProbe.rightClick(minerals, false);
                        break;
                    }
                }
            }
        }

        switch (buildOrderNumber) {
            // 7 - Pylon at Pylon Position[1]
            case 7:
                if (!buildingExists(UnitTypes.Protoss_Pylon, pylonPosition) &&
                        mineralCount > 100) {
                    buildPylons(mineralCount, pylonPosition);
                }
                // If the Pylon is built then build probes!
                if (buildingExists(UnitTypes.Protoss_Pylon, pylonPosition)) {
                    buildProbes(mineralCount);
                }
                break;
            // 10 - Forge[2]
            case 9:
                if (!buildingExists(UnitTypes.Protoss_Forge, forgePosition)) {
                    buildForge(mineralCount);
                }
                if (buildingExists(UnitTypes.Protoss_Forge, forgePosition)) {
                    buildProbes(mineralCount);
                }
                break;
            // 12 - two Photon Cannons[3]
            case 12:
                if (!buildingExists(UnitTypes.Protoss_Photon_Cannon, citadelPosition)) {
                    buildPhotonCannon(mineralCount, citadelPosition);
                }
                if (!buildingExists(UnitTypes.Protoss_Photon_Cannon, archivesPosition)) {
                    buildPhotonCannon(mineralCount, archivesPosition);
                }
                if (buildingExists(UnitTypes.Protoss_Photon_Cannon, archivesPosition)) {
                    buildProbes(mineralCount);
                }
                break;
            // 13 - One gateway to build[4]
            case 13:
                if (!buildingExists(UnitTypes.Protoss_Gateway, gatewayPosition)) {
                    buildGateway(mineralCount);
                }
                if (buildingExists(UnitTypes.Protoss_Gateway, gatewayPosition)) {
                    buildZealots(mineralCount);
                }
                break;
            // default, if there is a gateway, then build zealots. Else build probes.
            default:
                if (hasGateway) {
                    buildZealots(mineralCount);
                } else {
                    buildProbes(mineralCount);
                }
        }
//                  Future build order
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
    }
    /*--------------------------------------------------------------------
    |  Method buildingExists
    |
    |  Purpose: Assign the gateway to build a templar.
    |  Parameters: buildingType - The type of building.
    |              buildingPosition - The position of where to build the
    |                                 building.
    |  Returns: Boolean
    *-------------------------------------------------------------------*/
    private boolean buildingExists(UnitType buildingType, Position buildingPosition) {
        for (Unit u : bwapi.getMyUnits()) {
            if (u.getType() == buildingType){
                int pos1 = u.getPosition().getBX();
                int pos2 = buildingPosition.getBX();
                if (Math.abs(pos1 - pos2) <= 3) {
                    return true;
                }
            }
        }
        return false;
    }


    /*--------------------------------------------------------------------
    |  Method collectMinerals
    |
    |  Purpose: Find idle probes that are not our worker probe or gas
    |      probes to collect minerals. Since function is called in
    |      MatchFrame, it will constantly be checking if there are
    |      new probes warped and send them to collect gas.
    *-------------------------------------------------------------------*/
    public void collectMinerals(){
        for (Unit unit : bwapi.getMyUnits()) {
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
    }

    /*--------------------------------------------------------------------
    |  Method gasProbeCollect
    |
    |  Purpose: Have all probes but one worker probe to collect minerals
    |      until we build the assimilator. Then stop two probes from
    |      collecting minerals, assign them as gasprobes and start
    |      collecting gas.
    *-------------------------------------------------------------------*/
    public void collectGas(){
        if (hasAssimilator) {
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit == gasProbe) {
                    for (Unit refine : bwapi.getUnits(bwapi.getSelf())) {
                        if (refine.getType().isRefinery()) {
                            double distance = unit.getDistance(refine);
                            if (distance < 300) {
                                unit.gather(refine, true);
                                bwapi.drawCircle(unit.getPosition(), 8, BWColor.Yellow, true, false);
                                break;
                            }
                        }
                    }

                }
            }
        }
    }

    /*--------------------------------------------------------------------
    |  Method buildAssimilator
    |
    |  Purpose: Sends the worker probe to build an assimilator on top of
    |      the vespene gas when we do not have an assimilator.
    *-------------------------------------------------------------------*/
    public void buildAssimilator(int mineralCount){
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

    /*--------------------------------------------------------------------
    |  Method buildProbes
    |
    |  Purpose: Assign nexus to build a probe.
    *-------------------------------------------------------------------*/
    public void buildProbes(int mineralCount){
        if ( mineralCount >= 50 && !nexus.isTraining() &&
                bwapi.getSelf().getSupplyUsed()/2 <= bwapi.getSelf().getSupplyTotal()/2) {
            nexus.train(UnitTypes.Protoss_Probe);
        }
    }

    /*--------------------------------------------------------------------
    |  Method buildPylons
    |
    |  Purpose: Sends the worker probe to build a pylon.
    |  Parameter: pylonPosition - the position passed into the function
    |      to build a pylon.
    *-------------------------------------------------------------------*/
    public void buildPylons(int mineralCount, Position pylonPosition){
        if ( mineralCount >100){
            poolProbe.build(pylonPosition, UnitTypes.Protoss_Pylon);
        }
    }

    /*--------------------------------------------------------------------
    |  Method buildGateway
    |
    |  Purpose: Sends the worker probe to build a Gateway at the hardcoded position.
    |  Parameter: mineralCount - the current mineral count.
    *-------------------------------------------------------------------*/
    public void buildGateway(int mineralCount){
        if (mineralCount >150 && poolProbe.isIdle() ){
            poolProbe.build(gatewayPosition, UnitTypes.Protoss_Gateway);
            hasGateway = true;
        }
    }

    /*--------------------------------------------------------------------
    |  Method buildCyber
    |
    |  Purpose: Sends the worker probe to build a Cybernectic Core at the hardcoded position.
    |  Parameter: mineralCount - the current mineral count.
    *-------------------------------------------------------------------*/
    public void buildCyber(int mineralCount){
        if (hasGateway && mineralCount > 300){
            poolProbe.build(cyberPosition, UnitTypes.Protoss_Cybernetics_Core);
            hasCyber = true;
        }
    }

    /*--------------------------------------------------------------------
    |  Method buildCitadel
    |
    |  Purpose: Have the poolProbe build a Citadel at the hardcoded position.
    |  Parameter: mineralCount - the current mineral count.
    *-------------------------------------------------------------------*/
    public void buildCitadel(int mineralCount, int gasCount){
        if ( mineralCount > 350 && gasCount > 100 && poolProbe.isIdle()) {
            poolProbe.build(citadelPosition, UnitTypes.Protoss_Citadel_of_Adun);
            hasCitadel = true;
        }
    }

    /*--------------------------------------------------------------------
    |  Method buildTemplarArchive
    |
    |  Purpose: Have the poolProbe build a Templar Archive at the hardcoded position.
    |  Parameter: mineralCount - the current mineral count.
    *-------------------------------------------------------------------*/
    public void buildTemplarArchive(int mineralCount) {
        if (hasCitadel && mineralCount > 150 && poolProbe.isIdle()) {
            poolProbe.build(archivesPosition, UnitTypes.Protoss_Templar_Archives);
            hasArchives = true;
        }
    }

    /*--------------------------------------------------------------------
    |  Method buildForge
    |
    |  Purpose: Have the poolProbe build a Forge at the hardcoded position.
    |  Parameter: mineralCount - the current mineral count.
    *-------------------------------------------------------------------*/
    public void buildForge(int mineralCount) {
        if (mineralCount > 150 && poolProbe.isIdle()) {
            poolProbe.build(forgePosition, UnitTypes.Protoss_Forge);
            hasForge = true;
        }
    }

    // function to build photon canons
    public void buildPhotonCannon(int mineralCount, Position position) {
        if (mineralCount > 150 && poolProbe.isIdle()) {
            poolProbe.build(position, UnitTypes.Protoss_Photon_Cannon);
        }
    }

    /*--------------------------------------------------------------------
    |  Method buildDrag
    |
    |  Purpose: Assign the gateway with the smallest queue to build a dragoon.
    *-------------------------------------------------------------------*/
    public void buildDrag(int mineralCount, int gasCount){
        if(hasCyber && mineralCount > 300 && gasCount > 50){
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Protoss_Gateway) {
                    gateway = unit;
                    gateway.train(UnitTypes.Protoss_Dragoon);
                }
            }
        }
    }

    /*--------------------------------------------------------------------
    |  Method buildZealots
    |
    |  Purpose: Assign the gateway to build a zealot.
    *-------------------------------------------------------------------*/
    public void buildZealots(int mineralCount) {
        if (hasGateway) {
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Protoss_Gateway) {
                    gateway = unit;
                    gateway.train(UnitTypes.Protoss_Zealot);
                }
            }
        }
    }
    /*--------------------------------------------------------------------
    |  Method placement
    |
    |  Purpose: Assigns hardcoded positions to the Position variables
    *-------------------------------------------------------------------*/
    public void placement(){
        baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
        //  nexusPosition = nexus.getTilePosition();
        nexusPosition = nexus.getPosition();
        //System.out.print("This is  getTilePosition() " + nexusPosition);
        //System.out.print("This is  getPosition() " +nexusPosition);
        //getting the X Y positions of the nexus

        int xBuild = nexusPosition.getX(Position.PosType.PIXEL);
        int yBuild = nexusPosition.getY(Position.PosType.PIXEL);
        //System.out.print("X-BUILD " + xBuild);
        // System.out.print("Y-BUILD " + yBuild);

        //  System.out.print(newBuildingPosition);
        mineralPosition = minerals.getPosition();
        //getting the X Y coordinate of the mineral positions
        int xMin =  mineralPosition.getX(Position.PosType.PIXEL);
        int yMin= mineralPosition.getY(Position.PosType.PIXEL);

        // System.out.print(xMin);
        // System.out.print(yMin);
        int pixelPosiCounter = 50;
        int pixelNegaCounter = 50;
        if (xBuild > xMin){
            //nexus right of minerals so build to the right
            //Add X value
            xBuild = xBuild + 100 + pixelPosiCounter;

        }
        else{
            //nexus left of minerals build to left
            //Subtract X value
            xBuild = xBuild - 200;
        }
        if (yBuild > yMin){
            //nexus top of minerals so build to the top of nexus
            yBuild = yBuild + 100 + pixelPosiCounter;
        }
        else{
            //nexus bottom of minerals so build to the bottom of nexus
            //subtract Y value
            yBuild = yBuild - 200;
        }
        //euclidean distance to not build in this area
        pylonPosition = new Position(xBuild, yBuild);
        gatewayPosition = new Position(xBuild+70, yBuild+70);
        cyberPosition = new Position(xBuild-70, yBuild-70);
        citadelPosition = new Position(xBuild+90, yBuild-50);
        archivesPosition = new Position(xBuild-80, yBuild+40);
        forgePosition = new Position(xBuild-120, yBuild-50);
        pylonPosition2 = new Position(xBuild + 200, yBuild);
        pylonPosition3 = new Position(xBuild + 300, yBuild);
        pylonPosition4 = new Position(xBuild + 400, yBuild);

        bwapi.drawCircle(pylonPosition, 8, BWColor.White, true, false);
        bwapi.drawCircle(gatewayPosition, 8, BWColor.Green, true, false);
        bwapi.drawCircle(cyberPosition, 8, BWColor.Blue, true, false);
        bwapi.drawCircle(citadelPosition, 8, BWColor.Orange, true, false);
        bwapi.drawCircle(archivesPosition, 8, BWColor.Purple, true, false);
        bwapi.drawCircle(forgePosition, 8, BWColor.Red, true, false);
        bwapi.drawCircle(pylonPosition2, 8, BWColor.Cyan, true, false);

        bwapi.drawCircle(pylonPosition3, 8, BWColor.Red, true, false);
        //we dont want to create pylons too close to minerals, check the positioning of the mineral do avoid building in front of minerals
        //nexusPosition = nexus.getPosition();

    }

    @Override
    public void connected() {

    }

    @Override
    public void matchStart() {

    }

    @Override
    public void matchFrame() {

    }

    @Override
    public void matchEnd(boolean winner) {

    }

    @Override
    public void keyPressed(int keyCode) {

    }

    @Override
    public void sendText(String text) {

    }

    @Override
    public void receiveText(String text) {

    }

    @Override
    public void playerLeft(int playerID) {

    }

    @Override
    public void nukeDetect(Position p) {

    }

    @Override
    public void nukeDetect() {

    }

    @Override
    public void unitDiscover(int unitID) {

    }

    @Override
    public void unitEvade(int unitID) {

    }

    @Override
    public void unitShow(int unitID) {

    }

    @Override
    public void unitHide(int unitID) {

    }

    @Override
    public void unitCreate(int unitID) {

    }

    @Override
    public void unitDestroy(int unitID) {

    }

    @Override
    public void unitMorph(int unitID) {

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
