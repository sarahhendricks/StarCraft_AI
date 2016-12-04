package bot;

import java.util.HashSet;

import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
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
        }

        @Override
        public void matchFrame() {
                for (Unit u : bwapi.getAllUnits()) {
                        bwapi.drawCircle(u.getPosition(), 5, BWColor.Red, true, false);

                        //build the unit to build a pylon
                        if (bwapi.getSelf().getMinerals() >= 100 && poolProbe == null) {
                                for (Unit unit : bwapi.getMyUnits()){
                                        if (unit.getType() == UnitTypes.Protoss_Probe) {
                                                poolProbe = unit;
                                                break;
                                        }
                                }
                        }

                        if (bwapi.getSelf().getMinerals() >= 100) {
                                //build the assimilator
                                for (Unit unit : bwapi.getMyUnits()) {
                                        if (unit.getType() == UnitTypes.Protoss_Probe) {
                                               // poolProbe.build(unit.getPosition(), UnitTypes.Protoss_Pylon);
                                                for (Unit vespene : bwapi.getNeutralUnits()) {
                                                        if (vespene.getType().isResourceContainer() && vespene.getType() == UnitTypes.Resource_Vespene_Geyser) {
                                                       // if (vespene.getType() == UnitTypes.Resource_Vespene_Geyser) {
                                                               System.out.print("hello");
                                                       //         System.out.print(vespene.getPosition().getBY());
                                                              poolProbe.build(vespene.getTilePosition(), UnitTypes.Protoss_Assimilator);
                                                        }
                                                }
                                        }
                                }
                        }
                }

                for (Unit unit : bwapi.getMyUnits()) {
                        if (unit.getType() == UnitTypes.Protoss_Probe) {
                                // You can use referential equality for units, too
                                if (unit.isIdle()) {
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