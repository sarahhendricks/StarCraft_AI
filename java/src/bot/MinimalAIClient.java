package bot;

import java.util.HashSet;

import jnibwapi.*;
import jnibwapi.types.TechType;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.types.UpgradeType;
import jnibwapi.types.UpgradeType.UpgradeTypes;

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
        /** the prove that has been assigned to build */
        private Unit poolProbe;
        private Unit nexus;
        private Unit geyser;
        private Unit minerals;
        Position geyserPosition;
        Position nexusPosition;
        Position mineralPosition;
        Region baseRegion;

        boolean hasAssimilator = false;

        Position newBuildingPosition;

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

                for (Unit u : bwapi.getMyUnits()) {
                        if (u.getType() == UnitTypes.Protoss_Nexus) {
                                nexus = u;
                        } else if (u.getType() == UnitTypes.Protoss_Probe && poolProbe == null) {
                                poolProbe = u;
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
        }

        @Override
        public void matchFrame() {
                int mineralCount = bwapi.getSelf().getMinerals();
                buildAssimilator(mineralCount);
                collectMinerals();
                //System.out.print(hasAssimilator);
                buildProbes(mineralCount);
                buildPylons(mineralCount);
                placement();
        }

        //a function to collect Mineral
        public void collectMinerals(){
                for (Unit unit : bwapi.getMyUnits()) {
                        if (unit.getType() == UnitTypes.Protoss_Probe) {
                                // You can use referential equality for units, too
                                if (unit.isIdle() && unit != poolProbe) {
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
                                // you should make sure you have an assmilitor in construction before changing hasAssimilator.
                                hasAssimilator = true;
                        }
                }
                for (Unit u : bwapi.getAllUnits()) {
                        if (geyserPosition != null) {
                                bwapi.drawCircle(geyserPosition, 5, BWColor.Yellow, true, false);
                        }
                       // bwapi.drawCircle(u.getPosition(), 5, BWColor.Red, true, false);
                }
        }

        public void buildProbes(int mineralCount){
                for (Unit unit : bwapi.getMyUnits()){
                        if (unit.getType() == UnitTypes.Protoss_Nexus && mineralCount >= 50 && bwapi.getSelf().getSupplyUsed() < 16) {
                                unit.train(UnitTypes.Protoss_Probe);
                        }
                }
        }

        public void buildPylons(int mineralCount){
               // System.out.println(bwapi.getSelf().getSupplyUsed());
               // System.out.println(bwapi.getSelf().getSupplyTotal());
                if (bwapi.getSelf().getSupplyUsed() + 2 >= bwapi.getSelf().getSupplyTotal() && mineralCount >100){
                      //  System.out.println("Got supplies, start building pylons.");
                                //build the pylon
                                for (Unit unit : bwapi.getMyUnits()) {
                                        if (unit.getType() == UnitTypes.Protoss_Probe) {
                                                poolProbe.build(newBuildingPosition, UnitTypes.Protoss_Pylon);
                                        }
                                }
                        }
        }

        public void buildGateway(int mineralCount){

        }

        public Position placement(){

                baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
              //  nexusPosition = nexus.getTilePosition();
                nexusPosition = nexus.getPosition();
                //System.out.print("This is  getTilePosition() " + nexusPosition);
                //System.out.print("This is  getPosition() " +nexusPosition);
                int xBuild = nexusPosition.getX(Position.PosType.PIXEL);
                int yBuild = nexusPosition.getY(Position.PosType.PIXEL);
                //System.out.print("X-BUILD " + xBuild);
               // System.out.print("Y-BUILD " + yBuild);
                newBuildingPosition = new Position((xBuild + 200), (yBuild));
              //  System.out.print(newBuildingPosition);
                mineralPosition = minerals.getPosition();
                int xMin =  mineralPosition.getX(Position.PosType.PIXEL);
                int yMin= mineralPosition.getY(Position.PosType.PIXEL);
               // System.out.print(xMin);
               // System.out.print(yMin);

                if (xBuild > xMin){
                        //nexus right of minerals so build to the right
                        //Add X value
                        xBuild = xBuild+200;

                }
                else{
                        //nexus left of minerals build to left
                        //Subtract X value
                        xBuild = xBuild - 200;
                }
                if (yBuild > yMin){
                        //nexus top of minerals so build to the top of nexus
                        yBuild = yBuild - 200;
                }
                else{
                        //nexus bottom of minerals so build to the bottom of nexus
                        //subtract Y value
                        yBuild = yBuild - 200;
                }
                newBuildingPosition = new Position(xBuild, yBuild);

                bwapi.drawCircle(newBuildingPosition, 8, BWColor.White, true, false);
                bwapi.drawCircle(nexusPosition, 5, BWColor.Green, true, false);

                //we dont want to create pylons too close to minerals, check the positioning of the mineral do avoid building in front of minerals
                //nexusPosition = nexus.getPosition();
                return newBuildingPosition;

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