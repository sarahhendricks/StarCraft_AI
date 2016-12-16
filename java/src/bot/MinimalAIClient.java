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
        private Unit gasProbe3;
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

        Position center;
        Position firstChoke;
        Position secondChoke;

    //the attackCount is the number of units we plan to send to attack the enemy at a given time
        private int dragoonAttackCount = 6;
        private int zealotAttackCount = 6;
        private int positionCounter =0;

    //Queues for the buildings
         private int zealotQueue = 0;
         private int dragoonQueue = 0;

    //our base region variable
        Region baseRegion;

        //positioning for buildings
        Position pylonPosition;
        Position buildPosition;
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

                bwapi.setGameSpeed(0);
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

    /*--------------------------------------------------------------------
    |  Method protossVsPT
    |
    |  Purpose: Implement build order for protoss vs protoss and
    |      protoss vs terran according to specific supply counts and
    |      conditions. See gitHub readme for complete build order
    |      explanation.
    *-------------------------------------------------------------------*/
    private void protossVsPT(){
                // 4-8/9 Train Probes (8)
                if((supplyUsed/2) < 8 && mineralCount >=50){
                    buildProbes();
                }
                // 8/9 Build a pylon
                if((supplyUsed/2) == 8 && mineralCount >= UnitTypes.Protoss_Pylon.getMineralPrice()){
                    buildPylons(pylonPosition);
                }
                // 8-10/17 Train 2 probes (10)
                if((supplyTotal/2) > 9 && (supplyUsed/2) < 12 && mineralCount >= 50 && totalTrained(UnitTypes.Protoss_Probe) <11){
                    buildProbes();
                }
                // 10/17 Build gateway
                if((supplyUsed/2) >= 10 && mineralCount >= 150 && numType(UnitTypes.Protoss_Gateway)==0){
                    Position putPosition = placement(pylonPosition, 10, 400, 5);
                    bwapi.drawCircle(putPosition, 8, BWColor.Yellow, true, false);
                    buildGateway(putPosition);
                }
                // 12-16/17 Train 2 zealots (2)
                if((supplyUsed/2) >= 10 && mineralCount >= 100 && totalTrained(UnitTypes.Protoss_Zealot) < 4 && hasBuild(UnitTypes.Protoss_Gateway)){
                    buildZealots();
                }
                // 12/17 Build assimilator
                if((supplyUsed/2) >= 12 && mineralCount >= 100 && !hasBuild(UnitTypes.Protoss_Assimilator) && hasBuild(UnitTypes.Protoss_Gateway)){
                    buildAssimilator();
                }
                // collect gas
                collectGas();
                // 14/17 Build Cyber Core
                if((supplyUsed/2) >= 12 && mineralCount >= 200 && !hasBuild(UnitTypes.Protoss_Cybernetics_Core) && hasBuild(UnitTypes.Protoss_Assimilator)){
                    Position putPosition = placement(pylonPosition, 60, 160, 20);
                    buildCyber(putPosition);
                }
                //14-16/17 Make 2 Probes (12)
                if((supplyTotal/2) >= 17 && (supplyUsed/2) < 16 && mineralCount >= 50 && totalTrained(UnitTypes.Protoss_Probe) <13){
                    buildProbes();
                }
                // Build second Pylon at 16/17 supply
                 if((supplyUsed/2) >= 15 && mineralCount >= 100 && numType(UnitTypes.Protoss_Pylon)<2 && hasBuild(UnitTypes.Protoss_Cybernetics_Core)){
                     buildPylons(placement(pylonPosition, 200, 700, 50));
                 }
                // 17-19/25 Train 2 zealots (6)
                if((supplyUsed/2) >= 10 && mineralCount >= 100 && totalTrained(UnitTypes.Protoss_Zealot) < 2 && hasBuild(UnitTypes.Protoss_Gateway)){
                    buildZealots();
                }
                //6 zealots, go attack
                if((supplyUsed/2) >= 25 && totalTrained(UnitTypes.Protoss_Zealot)==6){
                    zealotsAttack(totalTrained(UnitTypes.Protoss_Zealot), 6);
                }
                // 17-19/25 Build gateway
                if((supplyUsed/2) >= 10 && mineralCount >= 150 && numType(UnitTypes.Protoss_Gateway)<2 && numType(UnitTypes.Protoss_Pylon)>=2){
                    Position usePosition = findPosition(UnitTypes.Protoss_Pylon, 2);
                    Position placeGate = placement(usePosition, 30, 500, 10);
                    buildGateway(placeGate);
                }
                // 16-10/17 Train 2 probes (10)
                if((supplyTotal/2) >= 18 && mineralCount >= 50 && totalTrained(UnitTypes.Protoss_Probe) <13){
                    buildProbes();
                }
                // Build second Pylon at 16/17 supply
                if((supplyUsed/2) >= 22 && mineralCount >= 100 && numType(UnitTypes.Protoss_Pylon)<5){
                    buildPylons(placement(pylonPosition, 200, 800, 50));
                }
                //Build Dragoon at Gateway at 15/17 supply at 125 minerals and 50 gas
                if((supplyUsed/2) >= 22 && mineralCount >= 125 && gasCount >= 50 && hasBuild(UnitTypes.Protoss_Cybernetics_Core) && totalTrained(UnitTypes.Protoss_Dragoon)<4){
                    buildDrag();
                }
                sendDrag();
                dragoonsAttack(totalTrained(UnitTypes.Protoss_Dragoon), 5);
                //Build Citadel
                if(mineralCount >= 150 && gasCount >= 100 && numType(UnitTypes.Protoss_Pylon) == 5 && !hasBuild(UnitTypes.Protoss_Citadel_of_Adun)){
                    Position usePosition = findPosition(UnitTypes.Protoss_Pylon, 2);
                    Position placeCitadel = placement(usePosition, 50, 120, 10);
                    if (placeCitadel != null) {
                        buildCitadel(placeCitadel);
                    }
                    else{
                        usePosition = findPosition(UnitTypes.Protoss_Pylon, 1);
                        placeCitadel = placement(usePosition, 50, 120, 10);
                        if (placeCitadel != null) {
                            buildCitadel(placeCitadel);
                        }
                    }
                }
                // Train 4 probes after Citadel
                if((supplyTotal/2) >= 18 && mineralCount >= 50 && totalTrained(UnitTypes.Protoss_Probe) <23 && hasBuild(UnitTypes.Protoss_Citadel_of_Adun)){
                    buildProbes();
                }
                //Build Templar
                if(mineralCount >= 150 && gasCount >= 200 && numType(UnitTypes.Protoss_Citadel_of_Adun) == 1 && !hasBuild(UnitTypes.Protoss_Templar_Archives)){
                    Position usePosition = findPosition(UnitTypes.Protoss_Pylon, 3);
                    Position placeTemplar = placement(usePosition, 50, 120, 5);
                    if (placeTemplar != null){
                        buildTemplarArchive(placeTemplar);
                    }
                    else{
                        usePosition = findPosition(UnitTypes.Protoss_Pylon, 2);
                        placeTemplar = placement(usePosition, 50, 120, 5);
                        if (placeTemplar != null){
                            buildTemplarArchive(placeTemplar);
                        }
                    }
                }
                if(mineralCount >= 125 && gasCount >= 100 && numType(UnitTypes.Protoss_Templar_Archives) == 1 && numType(UnitTypes.Protoss_Dark_Templar) < 3){
                    buildTemplar();
                }
                if(numType(UnitTypes.Protoss_Dark_Templar) > 0){
                    templarAttack(1, 1);
                }
        }

        /*--------------------------------------------------------------------
        |  Method protossVsZerg
        |
        |  Purpose: Implement build order for protoss vs zerg
        |      according to specific supply counts and
        |      conditions. See gitHub readme for complete build order
        |      explanation.
        *-------------------------------------------------------------------*/
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
                findChokePoint();
                //supply total
                supplyTotal = bwapi.getSelf().getSupplyTotal();
                dragoonAttackCount = 6;
                zealotAttackCount = 3;
                //Queues for the buildings
                zealotQueue = 0;
                dragoonQueue = 0;

                collectMinerals();
                collectGas();
                gasProbeCollect();
                beingAttacked();

                //branching off into our enemy-specific games
                if ((enemy == RaceType.RaceTypes.Protoss) || (enemy == RaceType.RaceTypes.Terran)) {
                        protossVsPT();
                }
                else {
                        protossVsZerg();
                }

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
                                // You can use referential equality for units, too
                                if (unit.isIdle() && unit != poolProbe && unit != gasProbe && unit != gasProbe2 && unit != gasProbe3) {
                                        for (Unit minerals : bwapi.getNeutralUnits()) {
                                                baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
                                                if (minerals.getType().isMineralField() && bwapi.getMap().getRegion(minerals.getPosition()) == baseRegion) {
                                                        double distance = unit.getDistance(minerals);
                                                        if (distance < 300) {
                                                                unit.gather(minerals, false);
                                                                //claimedMinerals.add(minerals);
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
        public void gasProbeCollect(){
            //everyone collect minerals
            if (hasBuild(UnitTypes.Protoss_Assimilator)){
                for (Unit u : bwapi.getMyUnits()){
                    if (u.getType() == UnitTypes.Protoss_Probe && u != poolProbe && gasProbe == null){
                        u.stop(false);
                        gasProbe = u;
                    }
                    else if(u.getType() == UnitTypes.Protoss_Probe && u != poolProbe && gasProbe2 == null){
                        u.stop(false);
                        gasProbe2 = u;
                    }
                    else if(u.getType() == UnitTypes.Protoss_Probe && u != poolProbe && gasProbe3 == null){
                        u.stop(false);
                        gasProbe3 = u;
                    }
                }
            }
            else{
                collectMinerals();
            }
        }

        /*--------------------------------------------------------------------
        |  Method collectGas
        |
        |  Purpose: After the two gasprobes have been assigned, collectGas
        |      function sends the two gasprobes to collect gas from the
        |      assimilator. The function is constantly called in MatchFrame
        |      as the collectMinerals function.
        *-------------------------------------------------------------------*/
        public void collectGas(){
                if (hasBuild(UnitTypes.Protoss_Assimilator)) {
                        for (Unit unit : bwapi.getMyUnits()) {
                                if (unit == gasProbe || unit == gasProbe2 || unit ==gasProbe3) {
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
        public void buildAssimilator(){
                if (poolProbe != null && !hasBuild(UnitTypes.Protoss_Assimilator) && mineralCount >= 100) {
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
        public void buildProbes(){
            nexus.train(UnitTypes.Protoss_Probe);
        }

        public int totalTrained(UnitType typeCheck){
            int q = 0;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == typeCheck) {
                    q = unit.getTrainingQueueSize();
                }
            }
            return (q + numType(typeCheck));
        }

        /*--------------------------------------------------------------------
        |  Method buildPylons
        |
        |  Purpose: Sends the worker probe to build a pylon.
        |  Parameter: pylonPosition - the position passed into the function
        |      to build a pylon.
        *-------------------------------------------------------------------*/
        public void buildPylons(Position pylonPosition){
            poolProbe.build(pylonPosition, UnitTypes.Protoss_Pylon);
        }

        public Position findPosition(UnitType testType, int index){
            int count = 0;
            for (Unit u : bwapi.getMyUnits()) {
                if (u.getType() == testType) {
                    //System.out.println(index);
                    count += 1;
                    if (count == index){
                        //System.out.println(u.getPosition());
                        return u.getPosition();
                    }
                }
            }
            return null;
        }

        /*--------------------------------------------------------------------
        |  Method numType
        |
        |  Purpose: Count the number of a specific type of units. Increment
        |      the count as we build more units of the same type.
        |  Parameter: testType - a UnitType passed into the function
        |      for counting.
        |  Return: count - the total number of the units of the specified
        |      UnitType.
        *-------------------------------------------------------------------*/
        public int numType(UnitType testType){
            int count = 0;
            for (Unit u : bwapi.getMyUnits()) {
                if (u.getType() == testType) {
                    count += 1;
                }
            }
            return count;
        }

        /*--------------------------------------------------------------------
        |  Method hasBuild
        |
        |  Purpose: Check if we have initiated building a unit.
        |  Parameter: testType - a UnitType passed into the function for
        |      checking.
        |  Return: true - if the unit has been initiated
        |       false - if the unit has not been initiated
        *-------------------------------------------------------------------*/
        public boolean hasBuild(UnitType testType){
            if (numType(testType)>=1){
                return true;
            }
            else{
                return false;
            }
        }

        /*--------------------------------------------------------------------
        |  Method buildGateway
        |
        |  Purpose: Sends the worker probe to build an assimilator on top of
        |      the vespene gas when we do not have an assimilator.
        *-------------------------------------------------------------------*/
        public void buildGateway(Position putHere){
            poolProbe.build(putHere, UnitTypes.Protoss_Gateway);
        }

        /*--------------------------------------------------------------------
        |  Method buildCyber
        |
        |  Purpose: Sends the worker probe to build a Cybernectic Core at a
        |      position generated with the placement function.
        |  Parameter: putHere - a position generated by the placement function
        |      passed in as the position of Cybernectic Core
        *-------------------------------------------------------------------*/
        public void buildCyber(Position putHere){
            for (Unit u : bwapi.getMyUnits()){
                if(u.getType() == UnitTypes.Protoss_Gateway){
                    if (u.isCompleted()){
                        //if the gateway has been built then try to build
                        poolProbe.build(putHere, UnitTypes.Protoss_Cybernetics_Core);
                    }
                }
            }
        }

        /*--------------------------------------------------------------------
        |  Method buildCitadel
        |
        |  Purpose: Have the poolProbe build a Citadel
        *-------------------------------------------------------------------*/
        public void buildCitadel(Position putHere){
            poolProbe.build(putHere, UnitTypes.Protoss_Citadel_of_Adun);
        }

        /*--------------------------------------------------------------------
        |  Method buildTemplarArchive
        |
        |  Purpose: Have the poolProbe build a Templar Archive
        *-------------------------------------------------------------------*/
        public void buildTemplarArchive(Position putHere) {
            poolProbe.build(putHere, UnitTypes.Protoss_Templar_Archives);
        }

        /*--------------------------------------------------------------------
        |  Method buildDrag
        |
        |  Purpose: Assign the gateway with the smallest queue to build a dragoon.
        *-------------------------------------------------------------------*/
        public void buildDrag(){
            Unit gateBuild = bestGateway();
            if (gateBuild != null){
                gateBuild.train(UnitTypes.Protoss_Dragoon);
            }
        }

        public void findChokePoint(){
            baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
            if (bwapi.getMap().getRegion(nexus.getPosition()) == baseRegion) {
                nexusPosition = nexus.getPosition();
                int regionID = bwapi.getMap().getRegion(nexusPosition).getID();
                firstChoke = bwapi.getMap().getRegion(regionID).getChokePoints().iterator().next().getFirstSide();
                center = bwapi.getMap().getRegion(regionID).getChokePoints().iterator().next().getCenter();
                secondChoke = bwapi.getMap().getRegion(regionID).getChokePoints().iterator().next().getSecondSide();
                bwapi.drawCircle(firstChoke, 8, BWColor.Blue, true, false);
                bwapi.drawCircle(secondChoke, 8, BWColor.Teal, true, false);
                bwapi.drawCircle(center, 8, BWColor.Purple, true, false);
            }
        }

        public void sendDrag(){
            for(Unit dragoon : bwapi.getMyUnits()){
                if (dragoon.getType() == UnitTypes.Protoss_Dragoon && dragoon.isIdle()){
                    positionCounter += 1;
                    if (positionCounter == 1 || positionCounter == 2){
                        System.out.println("hold");
                        dragoon.move(center, false);
                        dragoon.holdPosition(true);
                        break;
                    }
                    else if(positionCounter == 3 ){
                        dragoon.move(firstChoke, false);
                    }
                }
            }
        }

        /*--------------------------------------------------------------------
        |  Method buildZealots
        |
        |  Purpose: Assign the gateway to build a zealot.
        *-------------------------------------------------------------------*/
        public void buildZealots() {
            Unit gateBuild = bestGateway();
            if (gateBuild != null){
                gateBuild.train(UnitTypes.Protoss_Zealot);
            }
        }

        /*--------------------------------------------------------------------
        |  Method bestGateway
        |
        |  Purpose: Compare the gateways and find the one with the smaller queue.
        |           max at three so then all of the resources aren't used
        *-------------------------------------------------------------------*/
        public Unit bestGateway(){
            Unit unitBest = null;
            int check;
            int max = 3;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Protoss_Gateway) {
                    check = unit.getTrainingQueueSize();
                    if(check < max){
                        unitBest = unit;
                    }
                }
            }
            return unitBest;
        }

        /*--------------------------------------------------------------------
        |  Method buildTemplar
        |
        |  Purpose: Assign the gateway to build a templar.
        *-------------------------------------------------------------------*/
        //function to create templars
        public void buildTemplar() {
            Unit gateBuild = bestGateway();
            if (gateBuild != null){
                gateBuild.train(UnitTypes.Protoss_Dark_Templar);
            }
        }

//making all of the zealots attack once we hit a threshold count of the zealots, zealots will sit idle until that number is reached
        public void zealotsAttack(int zealotCounter, int zealotAttackCount ) {

                for (Unit u : bwapi.getEnemyUnits()) {
                        enemyAttack = u;
                        enemyPosition = enemyAttack.getPosition();
                }
                //starts building when it starts to build the x zealot so we need to attack at x+1
                if (zealotCounter >= (zealotAttackCount)) {
                        for (Unit  zealot : bwapi.getMyUnits()) {
                                if (zealot.getType() == UnitTypes.Protoss_Zealot && zealot.isIdle()) {
                                        zealot.attack(enemyPosition, false);
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
                //starts building when it starts to build the x zealot so we need to attack at x+1
                if (dragoonCounter >= (dragoonAttackCount)) {
                        for (Unit  drag : bwapi.getMyUnits()) {
                                if (drag.getType() == UnitTypes.Protoss_Dragoon && drag.isIdle()) {
                                        drag.attack(enemyPosition, false);
                                        break;
                                }
                        }
                }
        }

        //making all of the zealots attack once we hit a threshold count of the zealots, zealots will sit idle until that number is reached
        public void templarAttack(int templarCounter, int templarAttackCount ) {

            for (Unit u : bwapi.getEnemyUnits()) {
                enemyAttack = u;
                enemyPosition = enemyAttack.getPosition();
            }
            //starts building when it starts to build the x zealot so we need to attack at x+1
            if (templarCounter >= (templarAttackCount)) {
                for (Unit  templar : bwapi.getMyUnits()) {
                    if (templar.getType() == UnitTypes.Protoss_Dark_Templar && templar.isIdle()) {
                        templar.attack(enemyPosition, false);
                        break;
                    }
                }
            }
        }

        /*--------------------------------------------------------------------
        |  Method firstPylonPosition
        |
        |  Purpose: Set the position of the first pylon based on the x
        |      coordinate of the nexus and the minerals. If our nexus is on
        |      the right side of the minerals, we can conclude that we are
        |      on the left bottom corner of a map. We are then going to
        |      build the first pylon on the right side of the nexus.
        |      On the other hand, if our nexus is on the left side of the
        |      minerals, we can conclude that we are ont he right top corner
        |      of the map. We can then build the first pylon on the left side
        |      of the nexus.
        *-------------------------------------------------------------------*/
        public void firstPylonPosition(){

                baseRegion = bwapi.getMap().getRegion(nexus.getPosition());
                nexusPosition = nexus.getPosition();

                int xBuild = nexusPosition.getX(Position.PosType.PIXEL);
                int yBuild = nexusPosition.getY(Position.PosType.PIXEL);

                mineralPosition = minerals.getPosition();

                int xMin = mineralPosition.getX(Position.PosType.PIXEL);

                if (xBuild > xMin) {
                        //nexus right of minerals so build to the right
                        //Add X value
                        xBuild = xBuild + 300;

                } else {
                        //nexus left of minerals build to left
                        //Subtract X value
                        xBuild = xBuild - 300;
                }
                //euclidean distance to not build in this area
                pylonPosition = new Position(xBuild, yBuild);
                Position checkPosition = placement(pylonPosition, 0, 300, 10);
                if (pylonPosition != checkPosition && numType(UnitTypes.Protoss_Pylon) == 0){
                    pylonPosition = checkPosition;
                }
                bwapi.drawCircle(pylonPosition, 8, BWColor.White, true, false);
        }

        /*--------------------------------------------------------------------
        |  Method placement
        |
        |  Purpose: Based on the position of the pylons, check 8 points
        |      around the the pylon (spiraling out) and then check the
        |      radius of each point using the checkSpot function to make
        |      sure that the point is buildable.
        |  Parameter: start - the base position, the center of our spiral.
        |      radius - the starting radius of the spiral (the first circle)
        |      max - the maximum radius that placement function is going to
        |      check around the pylon
        |      inc - the number of pixels we are incrementing each time
        |      around.
        |  Return: buildPosition - a valid and buildable position to pass
        |      into other build functions.
        *-------------------------------------------------------------------*/
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

            buildPosition = null;
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
                        if (buildPosition != null){
                        break;
                        }
                }
                //System.out.println("Build Position = " + buildPosition);
                return buildPosition;
        }

    /*--------------------------------------------------------------------
    |  Method checkSpot
    |
    |  Purpose: Check eight points around each checkpoint generated by the
    |      placement function. If all eight points are buildable, then
    |      the corresponding checkpoint in the placement function is
    |      considered buildable and that position can be returned.
    |  Parameters: checkX - the x coordinate of the checkpoint the function
    |      is currently checking.
    |      checkY - the y coordinate of the chekcpoint the function is
    |      currently checking.
    |  Return: true - if all eight points around the checkpoint are
    |      buildable.
    |      false - if any of the eight points is not buildable.
    *-------------------------------------------------------------------*/
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

        public boolean beingAttacked() {
            for (Unit  unit : bwapi.getMyUnits()) {
                if (unit.isUnderAttack()) {
                    //System.out.print("HELP IM BEING ATTACKED");
                    return true;
                }
                else {
                    return false;
                }
            }
            return false;
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