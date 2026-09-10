# Build Hydra APK using only your phone

1. Create a GitHub account if you do not already have one.
2. Create a new repository, e.g. `HydraWater`.
3. Upload all files from this project ZIP to the repository. Keep `.github/workflows/build-apk.yml`.
4. Open the repository's **Actions** tab.
5. Select **Build Hydra APK**.
6. Tap **Run workflow**.
7. Wait for the workflow to finish.
8. Open the completed workflow run and scroll to **Artifacts**.
9. Download **Hydra-debug-apk** and extract it.
10. Install `app-debug.apk` on the phone.

The workflow uses a cloud runner to execute Gradle and upload the APK as an artifact.
