Gratuit-inator Android Adaptive Icon Package
============================================

Files:
- app/src/main/res/drawable-nodpi/ic_launcher_foreground.png
  Transparent adaptive foreground layer (432x432).
- app/src/main/res/values/colors.xml
  Navy adaptive-icon background color.
- app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
  Adaptive icon definition.
- app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
  Adaptive round icon definition.
- app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
  Legacy launcher fallback.
- Gratuit-inator_PlayStore_512.png
  512x512 square Play Store icon.

For an existing Android Studio project, copy the 'app/src/main/res' contents
into your project's app/src/main/res folder and use @mipmap/ic_launcher
as the application icon.

The adaptive icon intentionally uses the calculator + receipt without the
'Gratuit-inator' wordmark, because launcher icons are displayed very small.
The full wordmark remains in the Play Store listing icon.
