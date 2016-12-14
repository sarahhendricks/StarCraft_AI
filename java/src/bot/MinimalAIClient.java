package bot;


import java.util.HashSet;

import jnibwapi.*;
import jnibwapi.types.TechType;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
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

public class MinimalAIClient implements BWAPIEventListener {
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

        //positioning for buildings
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

        public MinimalAIClient() {
                bwapi = new JNIBWAPI(this, true);
                bwapi.start();
        }

        @Override
        public void connected() {}

        @Override
        public void matchStart() {
                System.out.println("Game Started");

                bwapi.enableUserInput();
                bwapi.enablePerfectInformation();

                bwapi.setGameSpeed(1);
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
                enemies = bwapi.getEnemies();
                for (Iterator<Player> it = enemies.iterator(); it.hasNext(); ) {
                        RaceType race = it.next().getRace();
                        if (race.equals(RaceType.RaceTypes.Protoss)) {
                                System.out.println("enemy is protoss");
                                enemy = RaceType.RaceTypes.Protoss;
                        }
                        else if (race.equals(RaceType.RaceTypes.Zerg)) {
                                System.out.println("enemy is zerg");
                                enemy = RaceType.RaceTypes.Zerg;
                        }
                        else {
                                System.out.println("enemy is terran");
                                enemy = RaceType.RaceTypes.Terran;
                        }
                }

                int closestChokePoint = 1000;
                for (ChokePoint chokePoint : bwapi.getMap().getChokePoints())
                {
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
         * The game strategy for Terran enemies.
         */
        private void protossVsTerran() {
        }

        /*
         * The game strategy for Zerg enemies.
         */
        private void protossVsZerg() {
                int mineralCount = bwapi.getSelf().getMinerals();
                int gasCount = bwapi.getSelf().getGas();
                // supply used
                int supplyUsed = bwapi.getSelf().getSupplyUsed();
                //supply total
                int supplyTotal = bwapi.getSelf().getSupplyTotal();

                int buildOrderNumber = supplyUsed / 2;

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
                        // 8 - Pylon at Natural Expansion[1]
                        case 7:
                                if (!buildingExists(UnitTypes.Protoss_Pylon, pylonPosition) &&
                                        mineralCount > 100) {
                                        buildPylons(mineralCount, pylonPosition);
                                }
                                if (buildingExists(UnitTypes.Protoss_Pylon, pylonPosition)) {
                                        buildProbes(mineralCount);
                                }
                                break;
                        case 8:
                                buildProbes(mineralCount);
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
                        case 10:
                                buildProbes(mineralCount);
//                                poolProbe.move(myChokePoint.getCenter(), false);
                                break;
                        case 11:
                                buildProbes(mineralCount);
                                break;
                        // 13 - two Photon Cannons[3]
                        case 12:
//                                Position chokePoint = myChokePoint.getCenter();
//                                int xBuild = chokePoint.getX(Position.PosType.PIXEL);
//                                int yBuild = chokePoint.getY(Position.PosType.PIXEL);
//                                Position pylonPoint = new Position(xBuild + 100, yBuild + 100);
//                                Position change = new Position(xBuild + 150, yBuild + 70);
//                                bwapi.drawCircle(pylonPoint, 8, BWColor.Green, true, false);
//                                bwapi.drawCircle(change, 8, BWColor.Cyan, true, false);

//                                if (poolProbe.isIdle() && poolProbe.getPosition().getBX() !=
//                                        pylonPoint.getBX()) {
//                                        System.out.println("pool probe idle");
////                                        for (Unit u : bwapi.getMyUnits()) {
////                                                if (u.getType() == UnitTypes.Protoss_Pylon)
////                                                        System.out.println(u.getID()+", "+u.getType());
////                                        }
//                                        poolProbe.move(pylonPoint, false);
//                                }
//                                if (!buildingExists(UnitTypes.Protoss_Pylon, pylonPoint)) {
//                                        System.out.println("no pylon exists yet.");
//                                        buildPylons(mineralCount, pylonPoint);
//                                }
//                                System.out.println(bwapi.getMap().isBuildable(change));
//                                System.out.println(buildingExists(UnitTypes.Protoss_Photon_Cannon, change));
                                if (!buildingExists(UnitTypes.Protoss_Photon_Cannon, citadelPosition)) {
                                        buildPhotonCannon(mineralCount, citadelPosition);
                                }
                                if (!buildingExists(UnitTypes.Protoss_Photon_Cannon, archivesPosition)) {
                                        buildPhotonCannon(mineralCount, archivesPosition);
                                }
                                if (buildingExists(UnitTypes.Protoss_Photon_Cannon, gatewayPosition)) {
                                        buildGateway(mineralCount);
                                }
                                buildZealots(mineralCount);
                                break;
                        case 13:
                                buildProbes(mineralCount);
                                break;
                        case 14:
                                
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
                placement();
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
        }

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
                // branching off into our enemy-specific games
                if (enemy == RaceType.RaceTypes.Protoss) {
                        protossVsProtoss();
                }
                else if (enemy == RaceType.RaceTypes.Terran) {
                        protossVsTerran();
                }
                else {
                        protossVsZerg();
                }
        }

        //a function to collect Mineral
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

        // a Function to collect Gas
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

        //a function to build the assimilator
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

        //function to build probes
        public void buildProbes(int mineralCount){
                if ( mineralCount >= 50 && !nexus.isTraining() &&
                        bwapi.getSelf().getSupplyUsed()/2 <= bwapi.getSelf().getSupplyTotal()/2) {
                        nexus.train(UnitTypes.Protoss_Probe);
                }
        }

        //function to build pylons
        public void buildPylons(int mineralCount, Position pylonPosition){
                if ( mineralCount >100){
                        poolProbe.build(pylonPosition, UnitTypes.Protoss_Pylon);
                }
        }

        //function to create a gateway
        public void buildGateway(int mineralCount){
                if (mineralCount >150 && poolProbe.isIdle() ){
                        poolProbe.build(gatewayPosition, UnitTypes.Protoss_Gateway);
                        hasGateway = true;
                        }
                }

        //function to create a cybernetics core
        public void buildCyber(int mineralCount){
                if (hasGateway && mineralCount > 300){
                        poolProbe.build(cyberPosition, UnitTypes.Protoss_Cybernetics_Core);
                        hasCyber = true;
                }
        }

        // function to build the citadel
        public void buildCitadel(int mineralCount, int gasCount){
                if ( mineralCount > 350 && gasCount > 100 && poolProbe.isIdle()) {
                        poolProbe.build(citadelPosition, UnitTypes.Protoss_Citadel_of_Adun);
                        hasCitadel = true;
                }
        }

        // function to build templar archive, needs citadel first
        public void buildTemplarArchive(int mineralCount) {
                if (hasCitadel && mineralCount > 150 && poolProbe.isIdle()) {
                        poolProbe.build(archivesPosition, UnitTypes.Protoss_Templar_Archives);
                        hasArchives = true;
                }
        }

        // function to build forge
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

        //function to create dragoons
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

        //function to create zealots
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

                bwapi.drawCircle(pylonPosition, 8, BWColor.White, true, false);
                bwapi.drawCircle(gatewayPosition, 8, BWColor.Green, true, false);
                bwapi.drawCircle(cyberPosition, 8, BWColor.Blue, true, false);
                bwapi.drawCircle(citadelPosition, 8, BWColor.Orange, true, false);
                bwapi.drawCircle(archivesPosition, 8, BWColor.Purple, true, false);
                bwapi.drawCircle(forgePosition, 8, BWColor.Red, true, false);
                //we dont want to create pylons too close to minerals, check the positioning of the mineral do avoid building in front of minerals
                //nexusPosition = nexus.getPosition();

        }
        public void terranEnemy(){
            //    if( int dragCount >9 && enemyTerran){
              //  }
        }
        public void protossEnemy(){
              //  if( dragCount >9 && enemyTerran){
              //  }
        }
        @Override
        public void keyPressed(int keyCode) {}
        @Override
        public void matchEnd(boolean winner) {}
        @Override
        public void sendText(String text) {}
        @Override
        public void receiveText(String text) {}
        @Override
        public void nukeDetect(Position p) {}
        @Override
        public void nukeDetect() {}
        @Override
        public void playerLeft(int playerID) {}
        @Override
        public void unitCreate(int unitID) {}
        @Override
        public void unitDestroy(int unitID) {}
        @Override
        public void unitDiscover(int unitID) {}
        @Override
        public void unitEvade(int unitID) {}
        @Override
        public void unitHide(int unitID) {}
        @Override
        public void unitMorph(int unitID) {}
        @Override
        public void unitShow(int unitID) {}
        @Override
        public void unitRenegade(int unitID) {}
        @Override
        public void saveGame(String gameName) {}
        @Override
        public void unitComplete(int unitID) {}
        @Override
        public void playerDropped(int playerID) {}
}