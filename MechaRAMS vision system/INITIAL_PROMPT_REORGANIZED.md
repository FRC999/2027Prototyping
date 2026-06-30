# Initial Project Prompt - Reorganized

Source document: `S:\MechaRAMS\Vision\MechaRAMS vision system\prompt.docx`

This file reorganizes the initial prompt for clarity without intentionally removing requirements.

## Context

Team 999 MechaRAMS is working on FRC robot code. The main problem identified is localization and trajectory driving.

Prior analysis was done using code and design notes from top 2025 and 2026 teams. The results are in `.md` and `.docx` documents in:

`S:\MechaRAMS\Vision\MechaRAMS vision system`

The final consolidated document is believed to be:

`FRC_AI_Automation_Master_Source_Guide_Portrait`

All other documents in that folder may be used, with document dates showing progression.

## Truthfulness Rule

Be truthful and direct. If something is not 100 percent known, ambiguous, or requires a question, stop and ask before proceeding. Do not guess. Otherwise, work unattended.

## Project Goal

Create robot code for the 2025 FRC robot chassis using:

- Two cameras connected over USB2.
- One Orange Pi.
- One Xbox controller.

If starting immediately with four cameras and two Orange Pis is technically better, say so early.

## Code Inputs

Use the 2026 full robot code for the latest drive template:

`T:\Projects\MechaRAMS\2026Competition\2026Competition`

This code is based on a CTRE template from the 2025-2026 season. The new test project drive code should be somewhat similar because it was easy to understand and modify.

If the standard CTRE template is better for this project, say so and use it instead. The current CTRE template is believed to be in:

`https://github.com/CrossTheRoadElec/Phoenix6-Examples/tree/main/java`

Relevant examples:

- `SwerveWithPathPlanner`
- `SwerveWithChoreo`

Use the 2025 code repository for CAN IDs and other chassis information:

`T:\Projects\MechaRAMS\2025Competition\C2025_release`

The project will run on the 2025 chassis.

## Code Output Location

Final project code goes in:

`S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration`

This is the base directory for the Java project. It currently contains an empty command robot template for 2026. Remove default commands, subsystems, and controllers unless reused.

## Documentation Output Location

Documentation, status files, markdown, docx, prompts, and similar artifacts go in:

`S:\MechaRAMS\2027Prototyping\MechaRAMS vision system`

Both the Java project and documentation folder are under the same Git/GitHub repository.

## Existing Documentation Sync

Older strategy documents are in:

`S:\MechaRAMS\2027Prototyping\MechaRAMS vision system`

Newer/current documents are in:

`S:\MechaRAMS\Vision\MechaRAMS vision system`

Compare the folders and update the repo-covered `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system` folder so the latest documents reach GitHub.

## Vision Requirements

Use good ideas from the researched teams and check for newer ideas.

PhotonVision is acceptable if appropriate. Custom AprilTag processing is also acceptable if better.

The testing setup should stay simple:

- Ideally one board with two AprilTags not far apart.
- Two or three boards are acceptable if necessary.
- Preference is one board.
- Use a small number of AprilTags.

Create a detailed plan with step-by-step and click-by-click instructions covering:

- Camera positioning.
- AprilTag positioning.
- Vision testing.
- Trajectory testing.

## Trajectory Requirements

If PathPlanner or Choreo paths need to be manually drawn, say so. Otherwise, create the proper files.

Trajectory and localization code should support simulation where possible, including vision simulation and AdvantageScope if supported. PhotonVision supports simulation.

## Drivetrain Characterization

The team probably did not do good drivetrain characterization on either robot.

If characterization is needed, include:

- The routines in code.
- The controls.
- Step-by-step and click-by-click explanation of how to run the routine.

## AI Collaboration Requirements

The project will be developed with both Claude and Codex.

Create appropriate:

- Skills.
- Configuration files.
- Session-state/task-tracking files.

Session-state information must be sufficient to switch between Claude and Codex for the next iteration.

Configuration files must instruct AIs to update session information before and after every task so switching engines is quick and efficient.

All mentor prompts should be put into a markdown document for future student analysis. This includes the initial prompt and future specified prompts.

The initial prompt should be reorganized for clarity without loss of meaning.

## Build and Dependencies

It is not required to compile or deploy because Gradle often fails due to missing libraries.

If possible, include third-party libraries such as CTRE in the project. If not possible, say what needs to be added.

## Controls Documentation

Create a final robot controls document covering:

- Which button does what.
- Any controls specific to data collection and analysis.

## Backup / GitHub

GitHub access with SSH certificate can be provided if auto-sync is needed for backup.

Do not assume GitHub access is already available.
