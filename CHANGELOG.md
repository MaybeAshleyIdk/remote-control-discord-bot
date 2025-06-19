<!-- markdownlint-disable no-duplicate-heading -->

# Changelog #

All notable changes to this project will be documented in this file.
The format is based on [**Keep a Changelog v1.0.0**](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [**Semantic Versioning v2.0.0**](https://semver.org/spec/v2.0.0.html).

## [v0.1.0-indev04] - 2025-06-19 ##

[v0.1.0-indev04]: <https://github.com/MaybeAshleyIdk/remote-control-discord-bot/releases/tag/v0.1.0-indev04>

### Fixed ###

* Fixed commands that haven't changed not working

## v0.1.0-indev03 - 2025-06-19 [WITHDRAWN] ##

### Changed ###

* Instead of deleting all slash commands and re-creating them on startup, the existing slash commands are queried:
  * Commands that haven't changed will not be touched
  * New commands are added
  * Commands whose name haven't changed are only edited (the command ID stays the same)
  * Commands that don't exist anymore are deleted

## [v0.1.0-indev02] - 2025-06-19 ##

[v0.1.0-indev02]: <https://github.com/MaybeAshleyIdk/remote-control-discord-bot/releases/tag/v0.1.0-indev02>

### Added ###

* Added custom logging calls

### Changed ###

* An SLF4J implementation has been added, which removes the warnings printed to stderr when the bot starts up and
  changes how JDA's logging calls look
* The `opus-java` dependency is excluded, making the resulting executable smaller by a few megabytes

## [v0.1.0-indev01] - 2025-06-15 ##

[v0.1.0-indev01]: <https://github.com/MaybeAshleyIdk/remote-control-discord-bot/releases/tag/v0.1.0-indev01>

Initial Release
