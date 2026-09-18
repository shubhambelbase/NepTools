# NepTools Release Signing

## Files

| File | Committed? | Purpose |
| --- | --- | --- |
| `release.keystore` | No (`.gitignore`) | The RSA 4096 release signing key. |
| `keystore.properties` | No (`.gitignore`) | Store/key passwords for the above. |
| `keystore.properties.template` | Yes | Shape of the properties file, for new machines and CI. |

`app/build.gradle.kts` reads `keystore.properties` from the project root. If it is missing or
incomplete the release build falls back to the **debug** key and prints a warning. A
debug-signed build must never be distributed: the Android debug keystore is public and is
identical on every developer machine.

## Back this up now

Losing either `release.keystore` or `keystore.properties` means you can never publish an update
for the existing application ID. Store both in a password manager or encrypted backup, not only
on this machine.

## Certificate fingerprint

```
SHA-256: 59:F9:5B:14:D6:BE:15:1E:03:F8:7B:7D:CE:59:CC:D7:3D:97:9D:03:4F:FB:E7:D4:D9:EF:7C:13:72:7D:66:A9
```

This value is pinned in `core/security/NepToolsSecurityGuard.kt` (`RELEASE_SIGNATURE_HASHES`).
The debug key fingerprint is allowed only in debug builds (`DEBUG_SIGNATURE_HASHES`).

## Migration notice for existing installs

Versions up to and including 2.6.1 were signed with the Android **debug** key. Android only
installs an update whose signature matches the installed app, so users running an older build
must uninstall before installing a release signed with the new key. Uninstalling erases local
data, so the upgrade instructions are:

1. Open NepTools, go to Settings, tap **Export Backup**, and save the JSON file somewhere safe.
2. Uninstall NepTools.
3. Install the new APK.
4. Open Settings, tap **Restore Backup**, and select the file from step 1.

Tell users this in the release notes. It is a one-time step.

## Google Play App Signing

If the app is ever published to Play, Google re-signs the APK with the Play signing key. Add
that certificate's SHA-256 to `RELEASE_SIGNATURE_HASHES` as well, otherwise the in-app integrity
check will report a signature failure on production installs.

## Rotating the key

```bash
keytool -genkeypair -v -keystore release.keystore -alias neptools-release \
  -keyalg RSA -keysize 4096 -validity 10950 \
  -dname "CN=NepTools, O=NepTools, L=Kathmandu, ST=Bagmati, C=NP"
```

Then update `storePassword`/`keyPassword` in `keystore.properties`, refresh the fingerprint in
`NepToolsSecurityGuard.kt`, and repeat the migration procedure above because the signature
changes again.

## CI

CI builds and tests the debug variant only. Release artifacts are built locally where the
keystore is available, or in CI with `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, and
`KEY_ALIAS`/`KEY_PASSWORD` secrets materialised into `keystore.properties`.
