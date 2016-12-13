package bot;


import java.util.HashSet;

import jnibwapi.*;
import jnibwapi.types.TechType;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType;

import java.util.Iterator;
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

        //booleans to check if a building exists
        boolean hasAssimilator = false;
        boolean hasGateway = false;
        boolean hasCyber = false;
        boolean hasCitadel = false;
        boolean hasArchives = false;

        //positioning for buildings
        Position pylonPosition;
        Position gatewayPosition;
        Position cyberPosition;
        Position citadelPosition;
        Position archivesPosition;
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

                for (Unit u : bwapi.getMyUnits()) {
                        if (u.getType() == UnitTypes.Protoss_Nexus) {
                                nexus = u;
                        } else if (u.getType() == UnitTypes.Protoss_Probe && poolProbe == null) {
                                poolProbe = u;
                        } else if (u.getType() == UnitTypes.Protoss_Probe && gasProbe == null) {
                                gasProbe = u;
                        }
                        // Print some position information, to understand how it works.
                   //     System.out.println(String.format("TYPE: %s\nPosition: %s\nTilePosition: %s\n", u.getType(), u.getPosition(), u.getTilePosition()));
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
                int mineralCount = bwapi.getSelf().getMinerals();
                int gasCount = bwapi.getSelf().getGas();
                // supply used
                int supplyUsed = bwapi.getSelf().getSupplyUsed();
                //supply total
                int supplyTotal = bwapi.getSelf().getSupplyTotal();

                //calling the functions in the matchframe
                buildAssimilator(mineralCount);
                collectMinerals();
                collectGas();
                //System.out.print(hasAssimilator);
                buildProbes(mineralCount);
                buildPylons(mineralCount, supplyUsed, supplyTotal);
                placement();
                pylonRadius();
                buildGateway(mineralCount);
               // buildCitadel(mineralCount, gasCount);
                buildCyber(mineralCount);
               // buildTemplarArchive(mineralCount);
               // buildDrag(mineralCount , gasCount);
                buildZealots(mineralCount);

        }

        //a function to collect Mineral
        public void collectMinerals(){

                for (Unit unit : bwapi.getMyUnits()) {
                        if (unit.getType() == UnitTypes.Protoss_Probe) {
                                // You can use referential equality for units, too
                                if (unit.isIdle() && unit != poolProbe && unit != gasProbe) {
                                        for (Unit minerals : bwapi.getNeutralUnits()) {
                                                baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
                                                if (minerals.getType().isMineralField() && bwapi.getMap().getRegion(minerals.getPosition()) == baseRegion) {
                                                        //& !claimedMinerals.contains(minerals)
                                                       // mineralPosition = minerals.getTilePosition();
                                                        //bwapi.drawCircle(mineralPosition, 8, BWColor.Red, true, false);
                                                     //   bwapi.drawBox();
                                                        double distance = unit.getDistance(minerals);
                                                        if (distance < 300) {
                                                                unit.rightClick(minerals, false);
                                                                //claimedMinerals.add(minerals);
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
                                // Get the geyser that's in our base.
                                baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
                                if (vespene.getType() == UnitTypes.Resource_Vespene_Geyser && bwapi.getMap().getRegion(vespene.getPosition()) == baseRegion) {
                                        // Use tile positions for building.
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
                //want to limit the numbeer of probes being built
                        if ( mineralCount >= 50 && bwapi.getSelf().getSupplyUsed() < 16) {
                                nexus.train(UnitTypes.Protoss_Probe);
                        }
        }

        //function to build pylons
        public void buildPylons(int mineralCount, int supplyUsed,int supplyTotal ){
                if (supplyUsed + 2 >= supplyTotal && mineralCount >100){
                                //build the pylon
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
        public void buildCitadel(int mineralCount, int gasCount){
                if ( mineralCount > 350 && gasCount > 100 && poolProbe.isIdle()) {
                        poolProbe.build(citadelPosition, UnitTypes.Protoss_Citadel_of_Adun);
                }
        }
        public void buildTemplarArchive(int mineralCount) {
                //need citadel made beforehand
                if (hasCitadel && mineralCount > 150 && poolProbe.isIdle()) {
                        poolProbe.build(archivesPosition, UnitTypes.Protoss_Templar_Archives);
                        hasArchives = true;
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
        //function trying to find the radius of the pylon
        public void pylonRadius() {
                for (Unit pylon : bwapi.getMyUnits()) {
                        if (pylon.getType() == UnitTypes.Protoss_Pylon) {
                                Position top = pylon.getTopLeft();
                              //  System.out.print("This is top left: "+top);
                              //  bwapi.drawBox(top, 5, BWColor.Blue, true, false);
                                Position bot = pylon.getBottomRight();
                               // System.out.print("This is bottom left: "+bot);
                                bwapi.drawBox(top, bot, BWColor.Yellow, true, false);
                        }
                }
        }

        public void placement() {

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
                int xMin = mineralPosition.getX(Position.PosType.PIXEL);
                int yMin = mineralPosition.getY(Position.PosType.PIXEL);

                // System.out.print(xMin);
                // System.out.print(yMin);
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
                gatewayPosition = new Position(xBuild + 70, yBuild + 70);
                cyberPosition = new Position(xBuild - 70, yBuild - 70);
                citadelPosition = new Position(xBuild + 90, yBuild - 50);
                archivesPosition = new Position(xBuild - 80, yBuild + 40);

                bwapi.drawCircle(pylonPosition, 8, BWColor.White, true, false);
                bwapi.drawCircle(gatewayPosition, 8, BWColor.Green, true, false);
                bwapi.drawCircle(cyberPosition, 8, BWColor.Blue, true, false);
                bwapi.drawCircle(citadelPosition, 8, BWColor.Orange, true, false);
                bwapi.drawCircle(archivesPosition, 8, BWColor.Purple, true, false);
                //we dont want to create pylons too close to minerals, check the positioning of the mineral do avoid building in front of minerals
                //nexusPosition = nexus.getPosition();

                int checkPointX = pylonPosition.getX(Position.PosType.PIXEL);
                int checkPointY = pylonPosition.getY(Position.PosType.PIXEL);
                Position checkPosition1;
                Position checkPosition2;
                Position checkPosition3;
                Position checkPosition4;
                Position checkPosition5;
                Position checkPosition6;
                Position checkPosition7;
                Position checkPosition8;


                int max = 1000;
                for (int x_offset = 0; x_offset < max; x_offset += 50) {
                        for (int y_offset = 0; y_offset <= (max); y_offset += 50) {
                                checkPosition1 = new Position(checkPointX + x_offset, checkPointY + y_offset);
                                checkPosition2 = new Position(checkPointX + x_offset, checkPointY - y_offset);
                                checkPosition3 = new Position(checkPointX - x_offset, checkPointY + y_offset);
                                checkPosition4 = new Position(checkPointX - x_offset, checkPointY - y_offset);
                                checkPosition5 = new Position(checkPointX, checkPointY + y_offset);
                                checkPosition6 = new Position(checkPointX, checkPointY - y_offset);
                                checkPosition7 = new Position(checkPointX + x_offset, checkPointY);
                                checkPosition8 = new Position(checkPointX - x_offset, checkPointY);

                                if (bwapi.isBuildable(checkPosition1, true)) {
                                        bwapi.drawCircle(checkPosition1, 3, BWColor.Red, true, false);
                                }
                                if (bwapi.isBuildable(checkPosition2, true)) {
                                        bwapi.drawCircle(checkPosition2, 3, BWColor.Yellow, true, false);
                                }
                                if (bwapi.isBuildable(checkPosition3, true)) {
                                        bwapi.drawCircle(checkPosition3, 3, BWColor.Orange, true, false);
                                }
                                if (bwapi.isBuildable(checkPosition4, true)) {
                                        bwapi.drawCircle(checkPosition4, 3, BWColor.Red, true, false);
                                }
                                if (bwapi.isBuildable(checkPosition5, true)) {
                                        bwapi.drawCircle(checkPosition5, 3, BWColor.Blue, true, false);
                                }
                                if (bwapi.isBuildable(checkPosition6, true)) {
                                        bwapi.drawCircle(checkPosition6, 3, BWColor.Orange, true, false);
                                }
                                if (bwapi.isBuildable(checkPosition7, true)) {
                                        bwapi.drawCircle(checkPosition7, 3, BWColor.Green, true, false);
                                }
                                if (bwapi.isBuildable(checkPosition8, true)) {
                                        bwapi.drawCircle(checkPosition8, 3, BWColor.Yellow, true, false);
                                }
                        }
                }
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