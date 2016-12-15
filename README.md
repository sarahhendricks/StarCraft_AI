# StarCraft_AI
An artificial intelligence agent for playing StarCraft Brood War.

This is a final team project for CSC-568 Artificial Intelligence.

## Project requirements:
Unlike Project 1, there are no individual roles. All team members are held equally responsible for all aspects of the project.
The basic capabilities of your agents should include:
 - Resource gathering for both minerals and vespene gas.
 - Ability to place buildings for construction.
 - Execution of at least one build order.
 - Ability to construct worker and combat units.
 - Micromanagement of units.
 - Being able to build and use  tier 1 units for each faction:
    - Terran: SCVs, marines and medics.
    - Zerg: Drones, zerglings and hydralisks.
    - Protoss: Probes, zealots, and dragoons.
 - Being able to build and use some tier 2 units for each faction:
    - Terran: Siege tanks, goliaths, vultures, or wraiths.
    - Zerg: Lurkers or mutalisks.
    - Protoss: Templars, dark templars, reavers, observers, or corsairs.

All deliverables should be available on GitHub. Required deliverables include:
 - Code that is clean, documented, and has appropriate class decomposition.
 - High-level overview of your approach (ballpark of 500 words in the README in the base of your repository).
 - Technical summary of your agent including a system diagram, explanation of all major classes, and a time-based guide as to what the agent does over time (also in the README).

## Code Structure
 One of our overarching goals in the project, was to divide our build paths into a decision tree based on the enemy which we were fighting. Our goal for the code base, was to keep the majority of `matchFrame()` clear of actual code and mainly filled with function calls.
 Therefore our major functions revolve around creation of buildings and units. They are as follows:
 - `collectMinerals` - Occurs throughout the games and has all non-assigned probes collect minerals
 - `collectGas` - Checks for the creation of an Assimilator (refinery) and if one such exists, assigns the gasProbe to harvest gas
 - `buildProbes` - Creates Probes, mainly used at the beginning of the game, but used throughout the build order
 - `buildPylons` - Builds Pylons based on positioning functions
 - `buildGateway` - Builds a Gateway based on positioning functions around a Pylon
 - `buildCyber` - Builds a Cybernetics Core
 - `buildCitadel` - Builds a Citadel
 - `buildTemplarArchive` - Builds a Templar Archive
 - `buildDrag` - Builds Dragoons
 - `buildZealots` - Builds Zealots
 - `pylonRadius` - Checks if buildings are within the region of a Pylon
 - `placement`
 - `checkSpot`

## Protoss V Protoss/Terran Build Order
![alt text](https://github.com/sarahhendricks/StarCraft_AI/images/PVP.png "Protoss V Protoss/Terran Build Order")

## Protoss V Zerg Build Order
![alt text](https://github.com/sarahhendricks/StarCraft_AI/images/PVZ.png "Protoss V Protoss/Terran Build Order")
