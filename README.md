# ConcurrencyCruise
A project utilizing concurrency to simulate passengers entering a ship and a captain ensuring the ship isnt overfilled.

Made with: Java, JavaFx, Java's built in concurrency mechanisms

The capacity of the bridge to the ship and the ship itself can be configured at the beginning of the simulation, otherwise the default values are taken from the config.xml file.
The passengers do not know the ship's capacity or the number of passengers on it, and will always try to enter when theres free space on the bridge to it.
Controlling the number of passengers entering and blocking the bridge during the cruise is done by the captain process, using semaphores.
Depending on the setting, after the cruise is finished the passengers on the ship will disappear or leave through the same bridge.
These processes are visualized using simple animations made with javaFX.
