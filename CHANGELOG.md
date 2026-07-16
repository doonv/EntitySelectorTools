# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html), well... as well as a Minecraft mod can adhere to semantic versioning, it's not a library.

## [Unreleased]

### Added

- Predicate rendering!
  - Supports both `execute if predicate` and `@e[predicate=...]`
- New "Copy Volume As Predicate Bounds" keybinding that copies the selected volume as a predicate `location_check` bounding box, for example:
  ```json
  "position": {
    "x": {
        "min": -8,
        "max": -55
    },
    "y": {
        "min": -14,
        "max": 1
    },
    "z": {
        "min": 6,
        "max": 4
    }
  }
  ```
- Added a new "Show Mod Missing Message" config option that disables the message that you get upon joining a server without Entity Selector Tools installed.
- Added a screen that shows up when client-side dependencies are missing.
  - This is a workaround for Fabric not having side-specific dependencies, but hey, it looks nice.

### Changed

- "Copy Creation" keybinding renamed to "Copy Volume As Selector"
- The keybinds shown in the volume creation tool are now shortened. (<kbd>Control</kbd> becomes <kbd>Ctrl</kbd>, and the spaces around <kbd>+</kbd> are removed)
- Made the copy keybindings use [Amecs](https://modrinth.com/project/rcLriA4v) instead of being hardcoded to use <kbd>Control</kbd>. This does mean **Entity Selector Tools depends on Amecs** for now. I do want to make it optional though, so I'm still working on that.
- Changed the color of inline code in the config screen to gold from light purple.
- If Axiom is loaded, its mouse icons are used instead of the name of the keybind.
- *Box Creation as Axiom Tool* is now enabled by default.
- Rewrote Axiom integration. 
- Several miscellaneous code improvements and renames.

### Fixed

- YACL being required on the server when it didn't need to be.

## [1.0.0]

Initial release.

[Unreleased]: https://github.com/doonv/entityselectortools/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/doonv/entityselectortools/releases/tag/1.0.0
