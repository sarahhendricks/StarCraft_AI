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

        Position geyserPosition;
        boolean hasAssimilator = false;

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
                        System.out.println(String.format("TYPE: %s\nPosition: %s\nTilePosition: %s\n", u.getType(), u.getPosition(), u.getTilePosition()));
                }


        }

        @Override
        public void matchFrame() {
                int mineralCount = bwapi.getSelf().getMinerals();
                buildAssimilator(mineralCount);
                collectMinerals();
        }

        //a function to collect Mineral
        public void collectMinerals(){
                for (Unit unit : bwapi.getMyUnits()) {
                        if (unit.getType() == UnitTypes.Protoss_Probe) {
                                // You can use referential equality for units, too
                                if (unit.isIdle() && unit != poolProbe) {
                                        for (Unit minerals : bwapi.getNeutralUnits()) {
                                                if (minerals.getType().isMineralField()) {
                                                        //& !claimedMinerals.contains(minerals)
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
                                Region baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
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
                                bwapi.drawCircle(geyserPosition, 1, BWColor.Yellow, true, false);
//                                System.out.println("Hello.");
                        }

                        bwapi.drawCircle(u.getPosition(), 5, BWColor.Red, true, false);

                }
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