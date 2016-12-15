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
        private Unit gasProbe2;
        private Unit nexus;
        private Unit enemyAttack;
        private Unit geyser;
        private Unit minerals;
        private Unit gateway;
        private Unit zealots;
        //neutral unit positions
        Position geyserPosition;
        Position nexusPosition;
        Position mineralPosition;
        Position enemyPosition;

    //counter for zealots
         private int zealotCounter = 0;
         private int dragoonCounter = 0;
    //the attackCount is the number of units we plan to send to attack the enemy at a given time
        private int dragoonAttackCount = 6;
        private int zealotAttackCount = 6;

    //Queues for the buildings
         private int zealotQueue = 0;
         private int dragoonQueue = 0;

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
        Position buildPosition;
        Position citadelPosition;
        Position archivesPosition;
        private Set<Player> enemies;
        private RaceType enemy;

        //Different counts we need
        public int mineralCount;
        private int gasCount;
        // supply used
        private int supplyUsed;
        //supply total
        private int supplyTotal;
        //probe count
        private int probeCount;

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

                //initializing all the variables
                bwapi.enableUserInput();
                bwapi.enablePerfectInformation();

                bwapi.setGameSpeed(1);
                poolProbe = null;
                gasProbe = null;
                gasProbe2 = null;
                zealots = null;

                for (Unit u : bwapi.getMyUnits()) {
                        if (u.getType() == UnitTypes.Protoss_Nexus) {
                                nexus = u;
                        } else if (u.getType() == UnitTypes.Protoss_Probe && poolProbe == null) {
                                poolProbe = u;
                        } else if (u.getType() == UnitTypes.Protoss_Probe && gasProbe == null) {
                                gasProbe = u;
                        } else if (u.getType() == UnitTypes.Protoss_Probe && gasProbe2 == null) {
                                gasProbe2 = u;
                        }
                }
                /*for (Unit attackUnits : bwapi.getMyUnits()){
                        if (attackUnits.getType() == UnitTypes.Protoss_Zealot && zealots == null) {
                                zealots = attackUnits;
                        }
                }*/

             /*  for (Unit u : bwapi.getEnemyUnits()) {
                        enemyAttack = u;
                        enemyPosition = u.getPosition();
                */
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
         *
         */
        private void protossVsPT(){
                // Build Probes
            //System.out.print("protoss terran");
                if((supplyUsed/2) < 8 && mineralCount >=50 && probeCount <9){
                        buildProbes();
                        probeCount += 1;
                }
//              // Collect minerals
                // Build a pylon at 8/9 supply && 100 minerals
                if((supplyUsed/2) == 8 && mineralCount >= 100){
                    buildPylons();
                }
                // Keep building probes
                if((supplyTotal/2) > 9 && (supplyUsed/2) < 12 && mineralCount >= 50 && probeCount <12){
                        buildProbes();
                        probeCount += 1;
                }
                // Gateway at 12/17 supply 150 minerals
                if((supplyUsed/2) >= 12 && mineralCount >= 150 && !hasGateway){
                    System.out.println("gateway");
                    gatewayPosition = placement(270, 50);
                    buildGateway();
                }
                //buildZealots();
                // Assimilator at 13/17 supply and 100 minerals
                if((supplyUsed/2) >= 13 && mineralCount >= 100 && !hasAssimilator){
                        //System.out.println("assimilator");
                        buildAssimilator();
                }
                // collect gas
                collectGas();
                // Build Dragoon at Gateway at 15/17 supply at 125 minerals and 50 gas
                if((supplyUsed/2) >= 14 && hasCyber && mineralCount >= 125 && gasCount >= 50){
                        System.out.println("Dragoon");
                        buildDrag();
                }
//              // Build Cyber Core at 15/17 supply && 200 minerals
                if((supplyUsed/2) >= 14 && !hasCyber && mineralCount >= 200){
                        System.out.println("cyber");
                        cyberPosition = placement(270, 50);
                        buildCyber();
                }
//                // Build second Pylon at 16/17 supply
                 if(supplyUsed == 16 && mineralCount >= 100){
                        buildPylons();
                 }
                     //butwo more zealots
        }

        /*
         * The game strategy for Zerg enemies.
         */
        private void protossVsZerg() {
            System.out.print("enemy is zerg");
        }

        /*
         * This runs every game frame (multiple times a second!!)
         */
        @Override
        public void matchFrame() {
                firstPylonPosition();
                mineralCount = bwapi.getSelf().getMinerals();
                gasCount = bwapi.getSelf().getGas();
                // supply used
                supplyUsed = bwapi.getSelf().getSupplyUsed();
                //supply total
                supplyTotal = bwapi.getSelf().getSupplyTotal();
               //counter for zealots
                zealotCounter = 0;
                dragoonCounter = 0;
                dragoonAttackCount = 6;
                zealotAttackCount = 3;
                //Queues for the buildings
                zealotQueue = 0;
                dragoonQueue = 0;

                collectMinerals();
                collectGas();

                //branching off into our enemy-specific games
                if ((enemy == RaceType.RaceTypes.Protoss) || (enemy == RaceType.RaceTypes.Terran)) {
                        protossVsPT();
                }
                else {
                        protossVsZerg();
                }

        }

        //a function to collect Mineral
        public void collectMinerals(){

                for (Unit unit : bwapi.getMyUnits()) {
                        if (unit.getType() == UnitTypes.Protoss_Probe) {
                                // You can use referential equality for units, too
                                if (unit.isIdle() && unit != poolProbe && unit != gasProbe && unit != gasProbe2) {
                                        for (Unit minerals : bwapi.getNeutralUnits()) {
                                                baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
                                                if (minerals.getType().isMineralField() && bwapi.getMap().getRegion(minerals.getPosition()) == baseRegion) {
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
                                if (unit == gasProbe || unit == gasProbe2) {
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
        public void buildAssimilator(){
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
        public void buildProbes(){
            nexus.train(UnitTypes.Protoss_Probe);
        }

        //function to build pylons
        public void buildPylons(){
            poolProbe.build(pylonPosition, UnitTypes.Protoss_Pylon);
        }

        //function to create a gateway
        public void buildGateway(){
            poolProbe.build(gatewayPosition, UnitTypes.Protoss_Gateway);
        }

        //function to create a cybernetics core
        public void buildCyber(){
            for (Unit u : bwapi.getMyUnits()){
                if(u.getType() == UnitTypes.Protoss_Gateway){
                    if (u.isCompleted()){
                        hasGateway = true;
                        poolProbe.build(cyberPosition, UnitTypes.Protoss_Cybernetics_Core);
                        hasCyber = true;
                    }
                }

            }
        }

//        //public void buildCitadel(){
//            poolProbe.build(citadelPosition, UnitTypes.Protoss_Citadel_of_Adun);
//        }

        public void buildTemplarArchive() {
            poolProbe.build(archivesPosition, UnitTypes.Protoss_Templar_Archives);
        }

        //function to create dragoons
        public void buildDrag(){
                if(hasCyber && mineralCount > 300 && gasCount > 50){
                        for (Unit unit : bwapi.getMyUnits()) {
                                if (unit.getType() == UnitTypes.Protoss_Gateway) {
                                        gateway = unit;
                                       // int dragoonQueue = gateway.getTrainingQueueSize();
                                     //   System.out.print(dragoonQueue);

                                        gateway.train(UnitTypes.Protoss_Dragoon);
                                }
                        }
                }
        }

        //function to create zealots
        public void buildZealots(int mineralCount, int zealotQueue, int zealotCounter) {
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Protoss_Gateway) {
                    gateway = unit;
                    //getting the size of the gateway warp queue
                    zealotQueue = gateway.getTrainingQueueSize();
                //zealotCounter not working
                    if (hasGateway && mineralCount > 100 && zealotQueue < 5 && zealotCounter <= 2) {
                        if(zealotCounter == 3 && zealotQueue != 0){
                            //cancel the remaining warp
                            System.out.print("cancel warp");
                        }
                        gateway.train(UnitTypes.Protoss_Zealot);
                    }
                }
            }
        }

        //function to create zealots
        public void buildTemplar( int mineralCount, int gasCount) {
                if (hasGateway && hasCitadel && hasArchives && mineralCount > 125 && gasCount > 100) {
                        for (Unit unit : bwapi.getMyUnits()) {
                                if (unit.getType() == UnitTypes.Protoss_Gateway) {
                                        gateway = unit;
                                        gateway.train(UnitTypes.Protoss_Dark_Templar);
                                }
                        }
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

//making all of the zealots attack once we hit a threshold count of the zealots, zealots will sit idle until that number is reached
        public void zealotsAttack(int zealotCounter, int zealotAttackCount ) {

                for (Unit u : bwapi.getEnemyUnits()) {
                        enemyAttack = u;
                        enemyPosition = enemyAttack.getPosition();
                }
                for (Unit unit : bwapi.getMyUnits()) {
                        if (unit.getType() == UnitTypes.Protoss_Zealot) {
                                zealotCounter = zealotCounter + 1;
                        }
                }
                //starts building when it starts to build the x zealot so we need to attack at x+1
                if (zealotCounter >= (zealotAttackCount  + 1)) {
                        for (Unit  zealot : bwapi.getMyUnits()) {
                                if (zealot.getType() == UnitTypes.Protoss_Zealot && zealot.isIdle()) {
                                      //  System.out.println("Atttackkkk PLEASE");
                                        zealot.attack(enemyPosition, false);
                                      //  System.out.println("Atttackkkk");
                                        break;
                                }
                        }
                }
        }

        //making all of the dragoons attack once we hit a threshold count of the dragoons
        public void dragoonsAttack(int dragoonCounter, int dragoonAttackCount) {
                for (Unit u : bwapi.getEnemyUnits()) {
                        enemyAttack = u;
                        enemyPosition = enemyAttack.getPosition();
                }
                for (Unit unit : bwapi.getMyUnits()) {
                        if (unit.getType() == UnitTypes.Protoss_Dragoon) {
                                dragoonCounter = dragoonCounter + 1;
                        }
                }
                //starts building when it starts to build the x zealot so we need to attack at x+1
                if (dragoonCounter >= (dragoonAttackCount +1)) {
                        for (Unit  drag : bwapi.getMyUnits()) {
                                if (drag.getType() == UnitTypes.Protoss_Dragoon && drag.isIdle()) {
                                        drag.attack(enemyPosition, false);
                                        break;
                                }
                        }
                }
        }


        public void firstPylonPosition(){

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

                //gatewayPosition = new Position(xBuild + 70, yBuild + 70);
                //cyberPosition = new Position(xBuild - 70, yBuild - 70);
                //citadelPosition = new Position(xBuild + 95, yBuild - 60);
                //archivesPosition = new Position(xBuild - 90, yBuild + 60);


                bwapi.drawCircle(pylonPosition, 8, BWColor.White, true, false);
                //bwapi.drawCircle(gatewayPosition, 8, BWColor.Green, true, false);
                //bwapi.drawCircle(cyberPosition, 8, BWColor.Blue, true, false);
                //bwapi.drawCircle(citadelPosition, 8, BWColor.Orange, true, false);
                //bwapi.drawCircle(archivesPosition, 8, BWColor.Purple, true, false);
                //we dont want to create pylons too close to minerals, check the positioning of the mineral do avoid building in front of minerals
                //nexusPosition = nexus.getPosition();
        }

        public Position placement(int max, int inc) {
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

            buildPosition = null;
                for (int x_offset = 80; x_offset < max; x_offset += inc) {
                        for (int y_offset = 80; y_offset <= (max); y_offset += inc) {
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
                        if (buildPosition != null){
                        break;
                        }
                }
                //System.out.println("Build Position = " + buildPosition);
                return buildPosition;
        }

        public boolean checkSpot(int checkX, int checkY){
                Position checkPosition1;
                Position checkPosition2;
                Position checkPosition3;
                Position checkPosition4;
                Position checkPosition5;
                Position checkPosition6;
                Position checkPosition7;
                Position checkPosition8;

                //max is the radius around things
                int max = 50;
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
                                if (bwapi.isBuildable(checkPosition1, true)&&bwapi.isBuildable(checkPosition2, true)&&bwapi.isBuildable(checkPosition3, true)&&bwapi.isBuildable(checkPosition4, true)&&bwapi.isBuildable(checkPosition5, true)&& bwapi.isBuildable(checkPosition6, true) && bwapi.isBuildable(checkPosition7, true) && bwapi.isBuildable(checkPosition8, true)) {

                                }
                                //else break and say it is not buildable
                                else{
                                        return false;
                                }
                        }
                }
                //return true since nothing returned false
                return true;
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