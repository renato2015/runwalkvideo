# Some proposals to improve ueye-nativelib #

  * Create a full screen window from JAVA code
  * If a handle is present, try to start a message pump without creating a window

  * Try to use Direct3D from the native code using the handle created on the java side (not applicable anymore)
  * Will need to try STEAL\_MODE for recording using D3D & AVI tools library
  * Analyse this solution with respect to a possible performance penalty

# Details #

  * Retrieve the GraphicsDevice instance for a current monitor
  * Create a new Frame and set it using setFullScreenWindow()
  * Get the native handle (HWND) for that Canvas using the Native JNA class
  * Add a parameter to startRunning() accepting a HWND handle
  * At native side, check if parameter is present
  * If present, try to render in Direct3D => impossible due to exclusivity of this mode to other rendering windows
  * If not present, proceed with code as it is now (creating a new window)