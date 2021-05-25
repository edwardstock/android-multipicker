# Release Notes

## 2.0.0
- Refactored to use with kotlin and androidx
- Added support for new ActivityResult API
- Fixed using external storage for android >= Q (api >= 29) as it does not give direct access to "sdcard" Pictures and DCIM.
- Fixed requesting permissions for android >= Q