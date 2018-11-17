## General
* Use your best judgement. Write code with readability being your top priority
* Document classes *only* after they're finalized and are not being actively worked on
* When working, stick to once specific task - This makes having multiple developers much easier to coordinate

## Multithreading
* All tasks & schedulers should be handled using the **Scheduler** class

## Logging
* All messages printed to the console should be accessed through the proper **Logger** class  
For Bukkit plugins, this means accessing **Logger** from **Commonms -> Bukkit**  
For Bungee plugins, access **Logger** from **Commons -> Bungee** instead

## Databases & Misc. Connections
* All classes that require a connection/disconnection to an external connection need to use the **Connectable** interface
* All database interactions should be handled using **DAOs**

## Pass/Fail Handling
* All pass/fail handling should be handled using the **Promise** package  
**Promise** should always offer a success response containing an object  
**FailablePromise** should always offer an object *if* successful, but can also fail containing a string as the error response  
**SimplePromise** does not return an object if successful, but should return an error response if it fails  

**Examples of Pass/Fail scenarios:**
* Literally anything running async with an expected response
* Performing multi-check actions such as renaming a faction

## Time
* Do **NOT** use `System.currentTimeMillis()`, instead, use `Time.now()`  
The **Time** utility class contains several handy methods for formatting time. **Use those whenever possible**

## Storing Locations
* Use **Locatables**, they offer quick methods to convert back-and-fourth from database documents and are thread-safe

## Custom Inventories/Menus
* Use the **Menu** class
At the time of writing this the Menu class is a bit inefficient in terms of code quality - I would like to rewrite this someday.

## Creating/Using Services
* Services are created by implementing **RiotService**. A service should be toggleable and will be considered a mini-plugin inside a RiotPlugin
* All Riot Services must be registered under the parent Riot Plugin in order to start/stop
* All commands/listeners related to the Riot Service should be registered using the RiotService methods

# W.I.P