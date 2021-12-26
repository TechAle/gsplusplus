# GS++ is discontinued, since 26-12-2021
<!-- PROJECT SHIELDS -->
[![license](https://img.shields.io/badge/License-GPL_v3.0-white.svg)](https://github.com/lukflug/gamesense-client/blob/master/LICENSE)
![minecraft](https://img.shields.io/badge/Minecraft-1.12.2-blue.svg)


<!-- TABLE OF CONTENTS -->
## Table of Contents
* [Information](#Information)
    * [About](#About)
* [gs++ Contributors](#gs++Contributors)
* [GameSense Contributors](#GameSenseContributors)
* [GameSense Credits](#GameSenseCredits)
* [Changelogs](#Changelogs)
* [From TechAle](#FromTechAle)


<!-- INFORMATION -->

#### About
GS++ (GameSense++) is a private Minecraft forge "utility born" for anarchy-related servers such as 2b2t.<br>
This project was started by TechAle after gamesense got discontinued.<br>

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
* Shaders (momentum)
* FootWalker (packet logged future and konas)
* ChorusPost (k5)
* Aspect (quantum)
* fix log exploit (https://github.com/ChloePrime/fix4log4j)
* AntiPing (Phobos)
* NewChunks (Seppuku)
* Trajectories (Phobos)
* Chams (k5)
* PacketLogger (w+3)
### gs++ changelogs
(thanks phantom)
## Modules Added: 
* Combat:<br>Anvil trap, AutoCrystalRewrite, AutoFeetPlace, AutoMend, Criticals, elevatot, Foot Walker, FootConcrete, Friends, Surround rewrite, AutoCreeper, AutoCity
* Exploits:<br>AutoDupe5B, BowExploit, ChorusPost, ClipFlight, CoordsExploit, Crasher, Mining spoof(CookieClient), PacketJump, No Break Reset, Anti gap fail, no rotate, PingSpoof, RoofInteract, Rubberband, Drown, newChunks, SpeedNom
* Movement:AirJump, Antihunger, Anti void, AutoWalk, Avoid, BoundsMove, ElytraFly, EntityControl, EntitySpeed, Fastfall, Flight, HighJump, HoleSnap, Jesus, LiquidSpeed, LongJump, PassiveSpeed, PhaseWalk, Pursue, SafeWalk, Scaffold, SlowFall, StepRewrite, Tickshift, Timer, Viewlock, WallClimb, ViewLock, Phase, LevitationControl, antiVoid, antiHunger
* Misc:<br>FastShulker, ExtraTab, IRC, MouseClickAction, Quiver, Spammer, Credits, KillEffect, PacketLogger, QueueNotifier
* Render:<br>Aspect, ClientTime, FOVMod, MobOwner, NoGlitchBlock, Predict, RainbowEnchant, Shaders, Swing, Ambience, mobOwner, Trails
* HUD:<br>Frames, LagNotifier

## Modules changed:
* Combat:<br>AutoArmor(Elytra Prioity), AutoSkull(Phase Stuff), AutoTrap(Silent swap), AutoWeb(Silent swap), Blocker(Backend Stuff), HoleFill(Silent Swap+Double hole), KillAura(Hit Delay+Other stuff), Offhand(Strict+Bias Health), SelfTrap(Silent swap), SelfWeb(Silent Swap), Surround(Center changes+Silent swap), KillAura (Renders)
* Exploits:<br>FastBreak(Tons if things), No Interact(Added only gapple), PacketXP(Moved to Combat section)
* Movement:<br>Anchor(Pitch+Fastfall), Blink(Made it better), PlayerTweaks(No slow strict+IceSpeed+PortalChat+updated guimove+NoWeb+NoFall), ReverseStep(Rewritten), Speed(Rewritten), Step(Improved NCP mode)
* Misc:<br>ChatModifier(Rewritten), ChatSuffix(Changed to GS++), DiscordRPC(Rewritten), FakePlayer(Moving fakeplayer+SimulatedDamage), HotbarRefill(Strict), NoEntityTrace(Added food and all), NoKick(signs edits)
* Render:<br>BlockHighlight(Rewritten), BreakESP(Rewritten), Capes(GS++,Amber), Chams(Pop chams), ESP(Color picker), FreeCam(Rewritten), HoleESP(RayTrace+NewColor settings), NameTags(Popchams compatibility), NoRender(No Weather), ShulkerViewer(Fixed 2.3.0 monkey), ViewModel(Rewritten), Chams (Skidded)
HUD:<br>Speedometer(Added Arrays)

# From TechAle
Hi, it's the main dev of gs++.<br>
Developing gs++ had been really funny and, for who is wondering, the release date was decided for the 27^ December, i have no idea why Sable decided to release it 4 days before lol<br>
Why am i releasing it? Well, basically every gs++ users are quitting, so, i see no reasons why keeping this private.<br>
It also got boring developing in minecraft, and the last things i coded were bad+speed coded.<br>
For the last 2 months the client become a little bit instable, mostly because of bad things i added.<br>
"gs++ is held together by hot glue" - Historian<br>
I dont suggest to continue gs++, it's really not worth lol, start a client from 0 that is 100%  better.<br>
After this, i'll quit minecraft developement, i wont fork/star anything that is related to minecraft.<br>
If you want to follow me, just know that i'll start creating things that are not for minecraft, mostly university.<br>
