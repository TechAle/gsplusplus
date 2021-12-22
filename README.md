# GS++
<!-- PROJECT SHIELDS -->
[![license](https://img.shields.io/badge/License-GPL_v3.0-white.svg)](https://github.com/lukflug/gamesense-client/blob/master/LICENSE)
![minecraft](https://img.shields.io/badge/Minecraft-1.12.2-blue.svg)


<!-- TABLE OF CONTENTS -->
## Table of Contents
* [Information]
    * [About]
    * [Questions]
* [gs++ Contributors]
* [GameSense Contributors]
* [GameSense Credits]
* [Changelogs]


<!-- INFORMATION -->

#### About
GS++ (GameSense++) is a private Minecraft forge "utility born" for anarchy-related servers such as 2b2t.<br>
This project was started by TechAle after gamesense got discontinued.<br>

#### Questions
* Why is this repository public? If you see this repository is because either you are a member
of gs++ gs++ got leaked and i decided that, if gs++ ever got leaked, i will make 
the source code public<br>
* I have an issue with gs++, can you help me? Unless you are a gs++ member, you wont get any support from me.
Remember that this is a private client that is different from open source<br>
* How can i get gs++? To be a member of gs++, you need to be vouched by most if not all members..<br>
* Why continuing as private client? I decided to continue developing gamesense as private because well, people are annoying.
The experience of gamesense was enough for me to make me say "No, never". So i decided to reserve my new updates
to only few people that have a little bit of brain.


#### GameSense Contributors
* Cyber (original developer)
* Hoosiers (original developer, head developer, maintainer)
* lukflug (color settings, OpenGL, ClickGUI and minor optimizations, issue tracker)
* auburnhud2 (whitespace and continuous integration, issue tracker)
* linustouchtips (TargetHUD, SkyColor, Misc Rendering)
* Dngerr (Blink and HotbarRefill)
* DarkiBoi (TotemPopCounter) 
* GL_DONT_CARE (Viewmodel Transformations)
* NekoPvP (First commit, Item FOV)
* TechAle (AutoAnvil, PistonCrystal, Blocker)
* 0b00101010 (HoleESP rewrite, misc render)
* A2H (Bug fixes, compatability)
* Xiaro (Code improvement, compatability, Rotation Manager)
* Soulbond (XCarry)
* Lyneez (NoFall)

#### GameSense Credits
***Check specific class files for full credits.***
This client started out based off of FINZ0's Osiris, which in turn was based off of 086's KAMI. 
Without these projects, GameSense would not exist so a lot of credit is due to them for their work.

This client also implements the ClickGUI using PanelStudio: https://github.com/lukflug/PanelStudio/.
If you believe that we are using your code and credits are not properly given, please message Techale#1195 on Discord.


#### gs++ Contributors
* TechAle (Main developer, head developer, mainteiner)
* Doogie13 (Misc modules)
* Mwa (Profile system and misc)
* Phantom826(new FPS counter
#### gs++ Credits
I think it's the duty of a developer to say if he took inspiration / took some piece of code from another client.
* How customchat is implemented for overriding  minecraft's chat (https://www.curseforge.com/minecraft/mc-mods/better-chat)
* BowExploit (https://github.com/PotatOoOoOo0/BowMcBomb)
* Shaders (mm+3)
* FootWalker (packet logged future and konas)
* ChorusPost (k5)
* Aspect (quantum)
* fix log exploit (https://github.com/ChloePrime/fix4log4j)
* AntiPing (Phobos)
* NewChunks (Seppuku)
* Trajectories (Phobos)
* Chams (k5)
### gs++ changelogs
(thanks phantom)
## Modules Added: 
* Combat:<br>Anvil trap, AutoCrystalRewrite, AutoFeetPlace, AutoMend, Criticals, elevatot, Foot Walker, FootConcrete, Friends, Surround rewrite
* Exploits:<br>AutoDupe5B, BowExploit, ChorusPost, ClipFlight, CoordsExploit, Crasher, Mining spoof(CookieClient), PacketJump, No Break Reset, Anti gap fail, no rotate, PingSpoof, RoofInteract, Rubberband
* Movement:AirJump, Antihunger, Anti void, AutoWalk, Avoid, BoundsMove, ElytraFly, EntityControl, EntitySpeed, Fastfall, Flight, HighJump, HoleSnap, Jesus, LiquidSpeed, LongJump, PassiveSpeed, PhaseWalk, Pursue, SafeWalk, Scaffold, SlowFall, StepRewrite, Tickshift, Timer, Viewlock
* Misc:<br>FastShulker, ExtraTab, IRC, MouseClickAction, Quiver, Spammer
* Render:<br>Aspect, ClientTime, FOVMod, MobOwner, NoGlitchBlock, Predict, RainbowEnchant, Shaders, Swing
* HUD:<br>Frames, LagNotifier

## Modules changed:
* Combat:<br>AutoArmor(Elytra Prioity), AutoSkull(Phase Stuff), AutoTrap(Silent swap), AutoWeb(Silent swap), Blocker(Backend Stuff), HoleFill(Silent Swap+Double hole), KillAura(Hit Delay+Other stuff), Offhand(Strict+Bias Health), SelfTrap(Silent swap), SelfWeb(Silent Swap), Surround(Center changes+Silent swap)
* Exploits:<br>FastBreak(Tons if things), No Interact(Added only gapple), PacketXP(Moved to Combat section)
* Movement:<br>Anchor(Pitch+Fastfall), Blink(Made it better), PlayerTweaks(No slow strict+IceSpeed+PortalChat+updated guimove+NoWeb+NoFall), ReverseStep(Rewritten), Speed(Rewritten), Step(Improved NCP mode)
* Misc:<br>ChatModifier(Rewritten), ChatSuffix(Changed to GS++), DiscordRPC(Rewritten), FakePlayer(Moving fakeplayer+SimulatedDamage), HotbarRefill(Strict), NoEntityTrace(Added food and all), NoKick(signs edits)
* Render:<br>BlockHighlight(Rewritten), BreakESP(Rewritten), Capes(GS++,Amber), Chams(Pop chams), ESP(Color picker), FreeCam(Rewritten), HoleESP(RayTrace+NewColor settings), NameTags(Popchams compatibility), NoRender(No Weather), ShulkerViewer(Fixed 2.3.0 monkey), ViewModel(Rewritten)
HUD:<br>Speedometer(Added Arrays)
