# Location-API-Exerciser

Small application to show the different location APIs on Android

# FusedLocationProviderClient

According to https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi the FusedLocationProviderApi (used by this application) is deprecated in favour of the FusedLocationProviderClient, https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient. 

However there is a warning in the Google location documentation, https://developer.android.com/training/location/retrieve-current.html, which states:
  
> Warning: Please continue using the FusedLocationProviderApi class and don't migrate to the FusedLocationProviderClient class until Google Play services version 12.0.0 is available, which is expected to ship in early 2018. Using the FusedLocationProviderClient before version 12.0.0 causes the client app to crash when Google Play services is updated on the device. We apologize for any inconvenience this may have caused
  
Therefore, this application has NOT been updated to use the FusedLocatinoProviderClient