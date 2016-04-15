This is the base SonarQ plugin for jQAssistant integration. To work this with plugin in SonaQ you have to realize missing functionality:
- add SonarQ specific rules for jQAssistant
- and map the jQAssistant id's to that rule id's at runtime (sensor run)

There are additional plugins providing that, so you have to combine always two of them exclusive:
- `sonarrules` + this base plugin
- `projectrules` + this base plugin

The plugin has been verified to work with SonarQ 5.1.2.